package com.galaxy.airviewdictionary.data.local.vision

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import com.galaxy.airviewdictionary.data.local.vision.model.Char
import com.galaxy.airviewdictionary.extensions._cutDecimal
import com.galaxy.airviewdictionary.extensions.isValid
import com.galaxy.airviewdictionary.data.remote.translation.Language
import com.galaxy.airviewdictionary.data.local.vision.model.Line
import com.galaxy.airviewdictionary.data.local.vision.model.Paragraph
import com.galaxy.airviewdictionary.data.local.vision.model.Transaction
import com.galaxy.airviewdictionary.data.local.vision.model.VisionResponse
import com.galaxy.airviewdictionary.data.local.vision.model.VisionSingleLineText
import com.galaxy.airviewdictionary.data.local.vision.model.Word
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.min
import kotlin.properties.Delegates


@Singleton
class VisionRepository @Inject constructor() {

    private val TAG = javaClass.simpleName

    private val textRecognitionClient: MyTextRecognizer = MyTextRecognizer(TextRecognizerType.TEXT)

    private val chineseRecognitionClient: MyTextRecognizer = MyTextRecognizer(TextRecognizerType.CHINESE)

    private val koreanRecognitionClient: MyTextRecognizer = MyTextRecognizer(TextRecognizerType.KOREAN)

    private val japaneseRecognitionClient: MyTextRecognizer = MyTextRecognizer(TextRecognizerType.JAPANESE)

    private val devanagariRecognitionClient: MyTextRecognizer = MyTextRecognizer(TextRecognizerType.DEVANAGARI)

    /**
     * Word 행 중심축 유사판단 + 높이 유사판단 최소 유사율.
     * (1에 가까울 수록 유사하다)
     */
    private var WORD_AXIS_FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO by Delegates.notNull<Double>()

    /**
     * Word 동일 Line 판단 {요소 간 거리 : 요소 폰트높이 평균} 비율 한계비.
     * (0에 가까울 수록 가깝다)
     */
    private var WORD_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT by Delegates.notNull<Double>()

    /**
     * Line 폰트높이 유사판단 최소 유사율.
     * (1에 가까울 수록 유사하다)
     */
    private var LINE_FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO by Delegates.notNull<Double>()

    /**
     * Line 동일 Paragraph 판단 텍스트 읽기 방향 최소 겹침 비율.
     */
    private var LINE_WRITE_DIRECTION_OVERLAP_MINIMUM_RATIO by Delegates.notNull<Double>()

    /**
     * Line 폰트높이 유사성 x 색상 유사성 x {행간 : 요소 폰트높이 평균} 비 affinity 한계비.
     */
    private var LINE_FONT_HEIGHT_COLOR_SPACING_AFFINITY_LIMIT by Delegates.notNull<Double>()

    /**
     * Line 행 중심축 유사판단 + 폰트높이 유사판단 최소 유사율.
     * (1에 가까울 수록 유사하다)
     */
    private var LINE_AXIS_HEIGHT_SIMILARITY_MINIMUM_RATIO by Delegates.notNull<Double>()

    /**
     * Line 동일 Line 판단 {요소 간 거리 : 요소 폰트높이 평균} 비율 한계비.
     * (0에 가까울 수록 가깝다)
     */
    private var LINE_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT by Delegates.notNull<Double>()

    fun addObserver(lifecycle: Lifecycle) {
        textRecognitionClient.addObserver(lifecycle)
        chineseRecognitionClient.addObserver(lifecycle)
        koreanRecognitionClient.addObserver(lifecycle)
        japaneseRecognitionClient.addObserver(lifecycle)
        devanagariRecognitionClient.addObserver(lifecycle)
    }

    suspend fun request(bitmap: Bitmap, sourceLanguageCode: String): VisionResponse = coroutineScope {
        Timber.tag(TAG).i("#### request() ####  $sourceLanguageCode")
        val inputImage: InputImage = InputImage.fromBitmap(bitmap, 0)

        try {
            // 조건에 맞는 TextRecognizer 를 사용하여 OCR 을 수행한다
            val deferredResults: List<Deferred<Text?>> = getRecognizers(sourceLanguageCode).map { recognizer ->
                async {
                    try {
                        Timber.tag(TAG).d("recognizer.detectorType : ${recognizer.type}")
                        val text: Text = recognizer.process(inputImage)
                        Timber.tag(TAG).d("_processSuspend text : ${text.text}")
                        text
                    } catch (e: Exception) {
                        Timber.tag(TAG).e("Error processing text recognition: ${e.message}")
                        null // 실패할 경우 null 반환
                    }
                }
            }

            val processResults: List<Text?> = deferredResults.awaitAll()

            // 여러개의 TextRecognizer 결과 중 confidence 가 가장 높은 결과를 취한다
            val text = processResults.maxByOrNull { text ->
                text?.textBlocks?.sumOf { block ->
                    block.lines.sumOf { line ->
                        line.confidence.toDouble()
                    }
                } ?: 0.0
            } ?: throw Exception("No text recognized")

            // OCR 결과를 Paragraphs 로 변환한다
            val (detectedLanguageCode, analyzedParagraphs) = withContext(Dispatchers.Default) {
                var _sourceLanguageCode = sourceLanguageCode
                if (sourceLanguageCode == "auto") {
                    _sourceLanguageCode = identifyLanguage(text.text)
                    Timber.tag(TAG).i("_sourceLanguageCode : $_sourceLanguageCode")
                }

                val isVerticalWriting = detectVerticalWriting(text)
                val writingDirection = Language.writingDirection(_sourceLanguageCode, isVerticalWriting)
                if (writingDirection == WritingDirection.LTR || writingDirection == WritingDirection.RTL) {
                    _sourceLanguageCode to textToParagraphs(bitmap, text, _sourceLanguageCode, writingDirection)
                } else {
                    _sourceLanguageCode to textToVerticalParagraphs(bitmap, text, _sourceLanguageCode, writingDirection)
                }
            }

            VisionResponse.Success(Transaction(bitmap, text, detectedLanguageCode, analyzedParagraphs))
        } catch (e: Exception) {
            // 예외 메시지 출력
            Timber.tag(TAG).e("Exception message: ${e.message}")
            Timber.tag(TAG).e("Stack trace:")
            e.printStackTrace()

            // 원인 분석
            val cause = e.cause
            if (cause != null) {
                Timber.tag(TAG).e("Cause: ${cause.message}")
                cause.printStackTrace()
            } else {
                Timber.tag(TAG).e("No underlying cause.")
            }

            VisionResponse.Error(e)
        }
    }

    /**
     * 조건에 맞는 TextRecognizer 목록
     */
    private fun getRecognizers(sourceLanguageCode: String): List<MyTextRecognizer> {
        return if (sourceLanguageCode == "auto") {
            listOf(
                textRecognitionClient,
                chineseRecognitionClient,
                koreanRecognitionClient,
                japaneseRecognitionClient,
                devanagariRecognitionClient
            )
        } else if (sourceLanguageCode.startsWith("zh")) {
            listOf(chineseRecognitionClient)
        } else if (sourceLanguageCode.startsWith("ko")) {
            listOf(koreanRecognitionClient)
        } else if (sourceLanguageCode.startsWith("ja")) {
            listOf(japaneseRecognitionClient)
        } else if (
            sourceLanguageCode == "mr" || // मराठी 마라티어 (Marathi)
            sourceLanguageCode == "sa" || // संस्कृत 산스크리트어 (Sanskrit)
            sourceLanguageCode == "hi" || // हिंदी 힌디어 (Hindi)
            sourceLanguageCode == "ne"    // नेपाली 네팔어 (Nepali)
        ) {
            listOf(devanagariRecognitionClient)
        } else {
            listOf(textRecognitionClient)
        }
    }

    /**
     * 주어진 텍스트의 언어를 ML Kit 으로 판정한다. 판정 불가 시 "und".
     * 자동 감지 번역에서 화면 전체가 아니라 실제 번역 대상 문장으로 감지할 때도 재사용한다.
     */
    suspend fun identifyLanguage(text: String): String = suspendCancellableCoroutine { continuation ->
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                continuation.resume(languageCode)
            }
            .addOnFailureListener { _ ->
                continuation.resume("und")
            }
    }

    /**
     * WritingDirection.LTR, WritingDirection.RTL
     */
    private fun textToParagraphs(bitmap: Bitmap, text: Text, sourceLanguageCode: String, writingDirection: WritingDirection): List<Paragraph> {
        Timber.tag(TAG).i("#### textToParagraphs() ####  ${"\n" + text.text}")
        setReferenceConstantValue(false, sourceLanguageCode)

        val elements: List<Text.Element> = convertTextLinesToTextElements(text.textBlocks.flatMap { textBlock -> textBlock.lines }, writingDirection)

        elements.forEach {
            Timber.tag(TAG).i("element : ${it.boundingBox} ${it.text} ${(it.boundingBox!!.width().toDouble() / it.boundingBox!!.height())._cutDecimal()}")
        }

        val words: List<Word> = convertTextElementsToWords(bitmap, elements, writingDirection)

        val lines: List<Line> = groupWordsIntoLines(bitmap, words, writingDirection)

        lines.forEach { Timber.tag(TAG).i("groupWordsIntoLines result : ${it.boundingBox}, ${it.representation}, ${it.words}") }

        var paragraphs: List<Paragraph> = groupLinesIntoParagraphs(lines, writingDirection)

        paragraphs.forEach { Timber.tag(TAG).i("groupLinesIntoParagraphs result : ${it.hasParallelLines} ${it.boundingBox} ${it.representation}") }

        /**
         * [groupLinesIntoParagraphs] 로 클러스터링 하는 경우 세로로 단락 구분이 되어 있는것을 감지하는 것이 어려우므로
         * 세로단락 구분을 [detectAndSplitParagraphs], [correctDetectAndSplitParagraphs] 으로 확인한다.
         */
        paragraphs = paragraphs.flatMap { paragraph ->
            val splitParagraphs = detectAndSplitParagraphs(paragraph, writingDirection)
            splitParagraphs.forEach {
                Timber.tag(TAG).d("detectAndSplitParagraphs ${it.boundingBox} ${it.representation} ")
            }
            val clusterParagraphs = correctDetectAndSplitParagraphs(splitParagraphs, writingDirection)
            clusterParagraphs.forEach {
                Timber.tag(TAG).d("correctDetectAndSplitParagraphs ${it.boundingBox} ${it.representation} ")
            }
            clusterParagraphs
        }

        paragraphs.forEach {
            Timber.tag(TAG).i("paragraphs ${it.boundingBox} ${it.representation} ")
        }

        return paragraphs
    }

    /**
     * WritingDirection.TTB_LTR, WritingDirection.TTB_RTL
     */
    private fun textToVerticalParagraphs(bitmap: Bitmap, text: Text, sourceLanguageCode: String, writingDirection: WritingDirection): List<Paragraph> {
        Timber.tag(TAG).i("#### textToVerticalParagraphs() ####  ${"\n" + text.text}")

        val textLines: List<Text.Line> =
            text.textBlocks
                .flatMap { textBlock ->
                    textBlock.lines.filter { it.boundingBox.isValid() }
                }.sortedWith(
                    Comparator { line1, line2 ->
                        val rightComparison = line2.boundingBox!!.right.compareTo(line1.boundingBox!!.right)
                        if (rightComparison != 0) rightComparison else line1.boundingBox!!.top.compareTo(line2.boundingBox!!.top)
                    }
                )

        textLines.forEach { Timber.tag(TAG).i("textLine : ${it.boundingBox}, ${it.text}") }

        val verticalLines = mutableListOf<Line>()
        val horizontalTextLines = mutableListOf<Text.Line>()

        textLines
            .filter { it.boundingBox.isValid() }
            .forEach { textLine ->
                if (textLine.boundingBox!!.height() > textLine.boundingBox!!.width()) {
                    val line = textLine._toLine(writingDirection).apply {
                        setFontAndBackgroundColors(bitmap)
                    }
                    verticalLines.add(line)
                } else {
                    horizontalTextLines.add(textLine)
                }
            }

        /** ####################################### verticalParagraphs ###################################### */
        setReferenceConstantValue(true, sourceLanguageCode)
        var verticalParagraphs: MutableList<Paragraph> = groupLinesIntoParagraphs(verticalLines, writingDirection).toMutableList()
        verticalParagraphs.forEach { Timber.tag(TAG).i("groupLinesIntoParagraphs result : ${it.boundingBox} ${it.representation}") }

        /**
         * [groupLinesIntoParagraphs] 로 클러스터링 하는 경우 세로로 단락 구분이 되어 있는것을 감지하는 것이 어려우므로
         * 세로단락 구분을 [detectAndSplitParagraphs] 으로 확인한다. (검증되지 않음)
         */
        verticalParagraphs = verticalParagraphs.flatMap { paragraph ->
            val splitParagraphs = detectAndSplitParagraphs(paragraph, writingDirection)
            splitParagraphs.forEach {
                Timber.tag(TAG).d("detectAndSplitParagraphs ${it.boundingBox} ${it.representation} ")
            }
            splitParagraphs
        }.toMutableList()

        /** ####################################### horizontalParagraphs ###################################### */
        setReferenceConstantValue(false, sourceLanguageCode)
        val horizontalWritingDirection = Language.writingDirection(sourceLanguageCode, false)
        val elements: List<Text.Element> = convertTextLinesToTextElements(horizontalTextLines, horizontalWritingDirection)
        val words: List<Word> = convertTextElementsToWords(bitmap, elements, horizontalWritingDirection)
        val lines: List<Line> = groupWordsIntoLines(bitmap, words, horizontalWritingDirection)
        var horizontalParagraphs: List<Paragraph> = groupLinesIntoParagraphs(lines, horizontalWritingDirection)
        horizontalParagraphs = horizontalParagraphs.flatMap { paragraph ->
            val splitParagraphs = detectAndSplitParagraphs(paragraph, horizontalWritingDirection)
            splitParagraphs.forEach {
                Timber.tag(TAG).d("detectAndSplitParagraphs ${it.boundingBox} ${it.representation} ")
            }
            val clusterParagraphs = correctDetectAndSplitParagraphs(splitParagraphs, horizontalWritingDirection)
            clusterParagraphs.forEach {
                Timber.tag(TAG).d("correctDetectAndSplitParagraphs ${it.boundingBox} ${it.representation} ")
            }
            clusterParagraphs
        }

        verticalParagraphs.forEach { Timber.tag(TAG).i("verticalParagraphs : ${it.boundingBox} ${it.representation}") }
        horizontalParagraphs.forEach { Timber.tag(TAG).i("horizontalParagraphs : ${it.boundingBox} ${it.representation}") }

        verticalParagraphs.addAll(horizontalParagraphs)

        return verticalParagraphs
    }

    /**
     * [com.google.mlkit.vision.text.Text.Line] 리스트 를
     * [com.google.mlkit.vision.text.Text.Element] 리스트로 변환한다.
     */
    private fun convertTextLinesToTextElements(textLines: List<Text.Line>, writingDirection: WritingDirection): List<Text.Element> {
        return when (writingDirection) {
            WritingDirection.LTR -> {
                textLines
                    .filter { it.boundingBox.isValid() }
                    .sortedWith(
                        Comparator { line1, line2 ->
                            val topComparison = line1.boundingBox!!.top.compareTo(line2.boundingBox!!.top)
                            if (topComparison != 0) topComparison else line1.boundingBox!!.left.compareTo(line2.boundingBox!!.left)
                        }
                    )
                    .flatMap { line -> line.elements }
            }

            WritingDirection.RTL -> {
                textLines
                    .filter { it.boundingBox.isValid() }
                    .sortedWith(
                        Comparator { line1, line2 ->
                            val topComparison = line1.boundingBox!!.top.compareTo(line2.boundingBox!!.top)
                            if (topComparison != 0) topComparison else line2.boundingBox!!.right.compareTo(line1.boundingBox!!.right)
                        }
                    )
                    .flatMap { line -> line.elements }
            }

            WritingDirection.TTB_LTR -> {
                textLines
                    .filter { it.boundingBox.isValid() }
                    .filter { it.boundingBox!!.width() < it.boundingBox!!.height() }
                    .sortedWith(
                        Comparator { line1, line2 ->
                            val leftComparison = line1.boundingBox!!.left.compareTo(line2.boundingBox!!.left)
                            if (leftComparison != 0) leftComparison else line1.boundingBox!!.top.compareTo(line2.boundingBox!!.top)
                        }
                    )
                    .flatMap { line -> line.elements }
            }

            WritingDirection.TTB_RTL -> {
                textLines
                    .filter { it.boundingBox.isValid() }
                    .filter { it.boundingBox!!.width() < it.boundingBox!!.height() }
                    .sortedWith(
                        Comparator { line1, line2 ->
                            val rightComparison = line2.boundingBox!!.right.compareTo(line1.boundingBox!!.right)
                            if (rightComparison != 0) rightComparison else line1.boundingBox!!.top.compareTo(line2.boundingBox!!.top)
                        }
                    )
                    .flatMap { line -> line.elements }
            }
        }
    }

    /**
     * [com.google.mlkit.vision.text.Text.Element] 리스트를
     * [Word] 리스트로 변환한다.
     */
    private fun convertTextElementsToWords(bitmap: Bitmap, elements: List<Text.Element>, writingDirection: WritingDirection): List<Word> {
        val words = mutableListOf<Word>()
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        for (element in elements) {
            element.boundingBox?.let { boundingBox ->
                // BoundingBox 보정 작업
                val left = if (boundingBox.left < 0) 0 else boundingBox.left
                val top = if (boundingBox.top < 0) 0 else boundingBox.top
                val right = if (boundingBox.right > bitmapWidth) bitmapWidth else boundingBox.right
                val bottom = if (boundingBox.bottom > bitmapHeight) bitmapHeight else boundingBox.bottom

                // 새로운 Rect 생성
                val correctedBoundingBox = Rect(left, top, right, bottom)

                // 보정된 boundingBox를 사용하여 너비와 높이를 확인
                if (correctedBoundingBox.width() > 0 && correctedBoundingBox.height() > 0) {
                    val chars = element.symbols
                        .filter { it.boundingBox.isValid() }
                        .map { Char(it.boundingBox!!, it.text, writingDirection) }

                    if (chars.isNotEmpty()) {
                        words.add(Word(correctedBoundingBox, element.text, writingDirection, chars))
                    }
                }
            }
        }
        return words
    }

    /**
     * [Word] 리스트를
     * [Line] 리스트로 변환한다.
     */
    private fun groupWordsIntoLines(bitmap: Bitmap, words: List<Word>, writingDirection: WritingDirection): List<Line> {
        val lines = mutableListOf<Line>()

        words
            .sortedWith(VisionSingleLineText.getComparator(writingDirection))
            .forEach { word ->
                var addedToLine = false

                for (line in lines) {
                    // 판단하려고 하는 새로운 Word 와 가장 근접한 line 의 Word
                    val closestWord = line.words.minByOrNull { it.getWriteDirectionDistance(word) }!!

                    // word-closestWord 폰트 높이 평균
                    val averageFontHeight: Double = word.getAverageFontHeight(closestWord)

                    // closestWord-word 중심축 거리
                    val axisDistance = word.getAxisDistance(closestWord)

                    //  중심축 거리가 closestWord-word 폰트 높이 평균 보다 크면 같은 라인이 아님
                    if (axisDistance > averageFontHeight) break

                    // 읽기방향 word-closestWord 거리
                    val writeDirectionDistance: Double = word.getWriteDirectionDistance(closestWord).toDouble()

                    // (요소 간 거리 : 요소 평균 높이) 비율
                    val writeDirectionDistanceFontHeightRatio: Double = writeDirectionDistance / averageFontHeight

                    /** 판단하려고 하는 새로운 Word 와 기존 Line 에서 새로운 Word 에 가장 근접한 Word 는 읽기방향 일정 거리 이상 떨어져 있지 않아야 한다. */
                    // [condition 0]
                    if (writeDirectionDistanceFontHeightRatio <= WORD_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT) { // 0.63
                        // 행방향 중심축 유사율
                        val axisSimilarityRatio = word.getAxisSimilarityRatio(closestWord)

                        // word-closestWord 폰트 높이 유사율
                        val fontHeightSimilarityRatio = word.getFontHeightSimilarityRatio(closestWord)

                        // 행방향 중심축 유사율 * word-closestWord 폰트 높이 유사율
                        val axisFontHeightSimilarityRatio = axisSimilarityRatio * fontHeightSimilarityRatio

                        /** 판단하려고 하는 새로운 Word 와 기존 Line 에서 새로운 Word 에 가장 근접한 Word 는 행방향으로 동일 선상에 위치하고, 폰트 높이가 유사해야 한다. */
                        // [condition 0-0]
                        if (axisFontHeightSimilarityRatio >= WORD_AXIS_FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO) { // 0.85
                            Timber.tag(TAG).d(
                                "groupWordsIntoLines add 0-0 : "
                                        + "${writeDirectionDistance._cutDecimal()}, "
                                        + "${averageFontHeight._cutDecimal()}, "
                                        + "${writeDirectionDistanceFontHeightRatio._cutDecimal()}, "
                                        + "${axisSimilarityRatio._cutDecimal()}, "
                                        + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                        + "${axisFontHeightSimilarityRatio._cutDecimal()}, "
                                        + "${line.representation}(${line.boundingBox}) + ${word.representation}(${word.boundingBox})"
                            )
                            line.addWord(word)
                            addedToLine = true
                            break
                        }
                        // [condition 0-1]
                        else {
                            Timber.tag(TAG).v(
                                "groupWordsIntoLines drop 0-1 : "
                                        + "${writeDirectionDistance._cutDecimal()}, "
                                        + "${averageFontHeight._cutDecimal()}, "
                                        + "${writeDirectionDistanceFontHeightRatio._cutDecimal()}, "
                                        + "${axisSimilarityRatio._cutDecimal()}, "
                                        + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                        + "${axisFontHeightSimilarityRatio._cutDecimal()}, "
                                        + "${line.representation}(${line.boundingBox}) + ${word.representation}(${word.boundingBox})"
                            )
                        }
                    }
                    // [condition 1]
                    else {
                        Timber.tag(TAG).v(
                            "groupWordsIntoLines drop 1 : "
                                    + "${writeDirectionDistance._cutDecimal()}, "
                                    + "${averageFontHeight._cutDecimal()}, "
                                    + "${writeDirectionDistanceFontHeightRatio._cutDecimal()}, "
                                    + "${line.representation}(${line.boundingBox}) + ${word.representation}(${word.boundingBox})"
                        )
                    }
                }

                if (!addedToLine) {
                    lines.add(0, Line(mutableListOf(word), writingDirection))
                }
            }

        lines.forEach {
            it.setFontAndBackgroundColors(bitmap)
        }

        return lines
    }

    /**
     * 분석된 Line 들을 Paragraph 로 클러스터링 한다.
     * 위에서 아래로, 왼쪽에서 오른쪽으로(LTR. RTL 은 반대) List<Line> 을 탐색하면서
     * 선행 Line 과 후행 Line 을 폰트높이, 라인간 거리, 배경색상, 폰트색상 등의 요소를 근거로 비교하고 클러스터링 한다.
     */
    private fun groupLinesIntoParagraphs(lines: List<Line>, writingDirection: WritingDirection): List<Paragraph> {
        val paragraphs = mutableListOf<Paragraph>()

        lines
            .sortedWith(VisionSingleLineText.getComparator(writingDirection))
            .forEach { line ->
                /** 판단하려고 하는 새로운 Line이 이미 분석되어 paragraphs 에 존재한다면 continue forEach loop */
                if (line in paragraphs.flatMap { it.lines }) return@forEach

                var addedToParagraph = false

                for (paragraph in paragraphs) {
                    // 텍스트 읽기 방향에서 일부 겹치는지의 여부
                    val isWriteDirectionOverlaps = line.isWriteDirectionOverlaps(paragraph)

                    // 줄바꿈 방향에서 일부 겹치는지의 여부
                    val isLineReturnDirectionOverlaps = line.isLineReturnDirectionOverlaps(paragraph)

                    /** 판단하려고 하는 새로운 라인과 기존 Paragraph 가 텍스트 읽기 방향과 줄바꿈 방향에서 일부 겹치면 동일 Paragraph 그룹으로 판단한다. */
                    if (isWriteDirectionOverlaps && isLineReturnDirectionOverlaps) {
                        Timber.tag(TAG).d(
                            "groupLinesIntoParagraphs add 0 : "
                                    + "${paragraph.boundingBox}, "
                                    + "${paragraph.representation}(${paragraph.height}), "
                                    + "${line.boundingBox}, "
                                    + "${line.representation}(${line.height})"
                        )

                        paragraph.lines.add(line)
                        addedToParagraph = true
                        break
                    }

                    // 판단하려고 하는 새로운 라인과 가장 근접한 paragraph 의 line (paragraph.lines 의 마지막 element)
                    val closestLine = paragraph.lines.lastOrNull() ?: continue // 없으면 continue

                    // line 과 closestLine 의 행간
                    val lineSpacing = closestLine.getLineReturnDirectionDistance(line)

                    // line 과 closestLine 의 행간이 closestLine 폰트높이보다 1.6배 이상 크면 같은 paragraph 가 아닌 것으로 판단함
                    if (lineSpacing > closestLine.fontHeight * 1.6) continue

                    // line-closestLine 폰트높이 평균
                    val averageFontHeight: Double = line.getAverageFontHeight(closestLine)

                    /** 판단하려고 하는 새로운 라인과 기존 Paragraph 내 라인들의 평균 폰트높이가 FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO 이상의 유사성을 가지고 있어야 한다. */
                    // line-closestLine 폰트높이 유사성
                    val fontHeightSimilarityRatio = line.getFontHeightSimilarityRatio(closestLine)

                    // [condition 0] 폰트높이 유사성 조건에 부합하는 경우
                    if (fontHeightSimilarityRatio >= LINE_FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO) {
                        // [condition 0-0] 텍스트 읽기 방향으로 일부 겹치는 경우
                        if (isWriteDirectionOverlaps) {
                            /** 판단하려고 하는 새로운 라인과 Paragraph 는 텍스트 읽기 방향으로 LINE_WRITE_DIRECTION_OVERLAP_MINIMUM_RATIO 비율 이상 겹쳐야 한다. */
                            // 텍스트 읽기 방향으로 width 가 작은 것이 큰 것에 겹치는 비율
                            val writeDirectionOverlapRatio: Double = line.getWriteDirectionOverlapRatio(paragraph)

                            // [condition 0-0-0] 텍스트 읽기 방향 겹침조건 부합하는 경우
                            if (writeDirectionOverlapRatio >= LINE_WRITE_DIRECTION_OVERLAP_MINIMUM_RATIO) {
                                /** closestLine 폰트높이의 유사성, 라인색상의 유사성과 라인 행간 affinity 를 이용하여 동일 Paragraph 를 판단한다. */
                                // 라인 font-background 색상 유사성
                                val colorSimilarity = closestLine.getColorSimilarity(line)

                                Timber.tag(TAG).d(
                                    "${String.format("#%08X", closestLine.fontColor)} "
                                            + "${String.format("#%08X", closestLine.backgroundColor)} "
                                            + "${String.format("#%08X", line.fontColor)} "
                                            + "${String.format("#%08X", line.backgroundColor)} "
                                )

                                // 라인 affinity ({행간 : 요소 평균 높이} 비)
                                val lineSpacingAffinity = min(1.0, 1.0 / (lineSpacing.toDouble() / averageFontHeight))

                                // 폰트높이 유사성 * 라인 font-background 유사성 * {행간 : 요소 평균 높이} 비 affinity
                                val fontHeightColorLineSpacingAffinity = fontHeightSimilarityRatio * colorSimilarity * lineSpacingAffinity

                                // [condition 0-0-0-0] 
                                if (fontHeightColorLineSpacingAffinity >= LINE_FONT_HEIGHT_COLOR_SPACING_AFFINITY_LIMIT) {
                                    Timber.tag(TAG).d(
                                        "groupLinesIntoParagraphs add 0-0-0-0 : "
                                                + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                                + "${colorSimilarity._cutDecimal()}, "
                                                + "${lineSpacingAffinity._cutDecimal()}, "
                                                + "*${fontHeightColorLineSpacingAffinity._cutDecimal()}, "
                                                + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height})"
                                    )
                                    paragraph.lines.add(line)
                                    addedToParagraph = true
                                    break
                                }
                                // [condition 0-0-0-1] 
                                else {
                                    Timber.tag(TAG).v(
                                        "groupLinesIntoParagraphs drop 0-0-0-1 : "
                                                + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                                + "${colorSimilarity._cutDecimal()}, "
                                                + "${lineSpacingAffinity._cutDecimal()}, "
                                                + "*${fontHeightColorLineSpacingAffinity._cutDecimal()}, "
                                                + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height})"
                                    )
                                }
                            }
                            // [condition 0-0-1] 
                            else {
                                Timber.tag(TAG).v(
                                    "groupLinesIntoParagraphs drop 0-0-1 : "
                                            + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                            + "${writeDirectionOverlapRatio._cutDecimal()}, "
                                            + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height})"
                                )
                            }
                        }

                        // [condition 0-1] 줄바꿈 방향에서 일부 겹치는 경우
                        else if (isLineReturnDirectionOverlaps) {
                            /** 판단하려고 하는 새로운 라인과 기존 Paragraph 에서 새로운 라인에 가장 근접한 라인은 줄바꿈 방향으로 동일 선상에 위치해야 한다. */
                            // 라인 중심축 유사율
                            val axisSimilarityRatio = line.getAxisSimilarityRatio(closestLine)

                            // 라인 중심축 유사율 * line-closestLine 높이 유사율
                            val axisHeightSimilarityRatio = axisSimilarityRatio * fontHeightSimilarityRatio

                            // [condition 0-1-0]
                            if (axisHeightSimilarityRatio >= LINE_AXIS_HEIGHT_SIMILARITY_MINIMUM_RATIO) {
                                /** 새로운 라인과 기존 Paragraph 에서 새로운 라인에 가장 근접한 라인은 텍스트 읽기 방향 일정 거리 이상 떨어져 있지 않아야 한다. */
                                // 텍스트 읽기 방향 Line-Line 거리
                                val writeDirectionDistance: Double = line.getWriteDirectionDistance(closestLine).toDouble()

                                // (요소 간 거리 : 요소 평균 폰트높이) 비율
                                val writeDirectionDistanceFontHeightRatio: Double = writeDirectionDistance / averageFontHeight

                                // [condition 0-1-0-0]
                                if (writeDirectionDistanceFontHeightRatio <= LINE_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT) {
                                    Timber.tag(TAG).d(
                                        "groupLinesIntoParagraphs add 0-1-0-0 : "
                                                + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                                + "${axisSimilarityRatio._cutDecimal()}, "
                                                + "${axisHeightSimilarityRatio._cutDecimal()}, "
                                                + "${writeDirectionDistance._cutDecimal()}, "
                                                + "${writeDirectionDistanceFontHeightRatio._cutDecimal()}, "
                                                + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height}))"
                                    )

                                    if (writingDirection == WritingDirection.LTR) {
                                        if (closestLine.boundingBox.right < line.boundingBox.right) {
                                            paragraph.lines.add(line)
                                        } else {
                                            paragraph.lines.add(paragraph.lines.size - 1, line)
                                        }
                                    } else {
                                        if (line.boundingBox.left < closestLine.boundingBox.left) {
                                            paragraph.lines.add(line)
                                        } else {
                                            paragraph.lines.add(paragraph.lines.size - 1, line)
                                        }
                                    }
                                    // 복수의 Line 들이 하나의 행을 이루게 된다
                                    paragraph.hasParallelLines = true
                                    addedToParagraph = true
                                    break
                                }
                                // [condition 0-1-0-1]
                                else {
                                    Timber.tag(TAG).v(
                                        "groupLinesIntoParagraphs drop 0-1-0-1 : "
                                                + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                                + "${axisSimilarityRatio._cutDecimal()}, "
                                                + "${axisHeightSimilarityRatio._cutDecimal()}, "
                                                + "${writeDirectionDistance._cutDecimal()}, "
                                                + "${writeDirectionDistanceFontHeightRatio}, "
                                                + "${LINE_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT}, "
                                                + "${(writeDirectionDistanceFontHeightRatio <= LINE_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT)}, "
                                                + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height}))"
                                    )
                                }
                            }
                            // [condition 0-1-1]
                            else {
                                Timber.tag(TAG).v(
                                    "groupLinesIntoParagraphs drop 0-1-1 : "
                                            + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                            + "${axisSimilarityRatio._cutDecimal()}, "
                                            + "${axisHeightSimilarityRatio._cutDecimal()}, "
                                            + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height}))"
                                )
                            }
                        }
                        // [condition 0-2]
                        else {
                            Timber.tag(TAG).v(
                                "groupLinesIntoParagraphs drop 0-2 : "
                                        + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                        + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height}))"
                            )
                        }
                    }
                    // [condition 1]
                    else {
                        Timber.tag(TAG).v(
                            "groupLinesIntoParagraphs drop 1 : "
                                    + "${fontHeightSimilarityRatio._cutDecimal()}, "
                                    + "${closestLine.representation}(${closestLine.height}) + ${line.representation}(${line.height}))"
                        )
                    }
                }

                if (!addedToParagraph) {
                    paragraphs.add(0, Paragraph(mutableListOf(line), writingDirection))
                }
            }

        return paragraphs
    }

    /**
     * groupLinesIntoParagraphs 에서 분석된 Paragraph 중
     *
     *        △ △ △ △ △ △ △ △  ○ ○ ○ ○ ○ ○ ○ ○
     *        △ △ △ △ △ △ △ △  ○ ○ ○ ○ ○ ○ ○ ○
     *        △ △ △ △ △ △ △ △  ○ ○ ○ ○ ○ ○ ○ ○
     *        △ △ △ △ △ △ △ △  ○ ○ ○ ○ ○ ○ ○ ○
     *
     * 이런 Paragraph 의 경우 △ 단락과 ○ 단락이 있으나, groupLinesIntoParagraphs 에서 분리해 내지 못한다.
     *
     * 복수의 Line 들이 하나의 행을 이루는 것이 있는 Paragraph 를 분석하여
     * 세로로 단락 구분이 가능한지 확인한다.
     * DBSCAN 기법을 이용하되, 요소 간 거리는 x축 기준으로 판단하여 Line 의 시작위치가 비슷한 것 끼리 클러스터링 한다.
     */
    private fun detectAndSplitParagraphs(paragraph: Paragraph, writingDirection: WritingDirection): List<Paragraph> {
        if (!paragraph.hasParallelLines || paragraph.lines.size == 1) {
            return listOf(paragraph)
        }

        // lines 가 하나의 라인을 이루는 경우
        if (paragraph.areAllInLine()) {
            return listOf(paragraph)
        }

        Timber.tag(TAG).d("detectAndSplitParagraphs ${paragraph.representation}")

        val lines = paragraph.lines
        val clustersVisited = mutableSetOf<Line>()
        val clusters = mutableListOf<MutableList<Line>>()
        val distanceLimit: Double = paragraph.averageLineHeight()

        // 클러스터 확장 및 탐색
        // 각 Line을 기준으로 이웃하는 Line을 찾고, 이를 클러스터에 추가하며 재귀적으로 탐색한다
        fun expandCluster(line: Line, cluster: MutableList<Line>) {
            val neighbors =
                lines.filter {
                    if (it != line) {
                        Timber.tag(TAG).i(
                            "Split cluster "
                                    + "$distanceLimit, ${abs(line.startPosition - it.startPosition)}, ${line.boundingBox}, ${line.representation}, ${it.boundingBox}, ${it.representation}"
                        )
                    }
                    it != line && abs(line.startPosition - it.startPosition) <= distanceLimit
                }

            cluster.add(line)
            clustersVisited.add(line)
            neighbors.forEach {
                if (!clustersVisited.contains(it)) {
                    expandCluster(it, cluster)
                }
            }
        }

        // 클러스터 그룹화
        lines.forEach { line ->
            if (!clustersVisited.contains(line)) {
                val cluster = mutableListOf<Line>()
                expandCluster(line, cluster)
                clusters.add(cluster)
            }
        }

        return clusters.map { cluster -> Paragraph(cluster.toMutableList(), writingDirection) }
    }

    /**
     * groupLinesIntoParagraphs 에서 분석된 Paragraph 중
     *
     *        ○ ○ ○ ○ ○ ○ ○ ○  ○ ○ ○ ○ ○ ○ ○ ○
     *            ○ ○ ○ ○ ○ ○ ○ ○ ○ ○ ○
     *                  ○ ○ ○ ○ ○ ○ ○ ○ ○ ○
     *
     * 이런 Paragraph 의 경우 detectAndSplitParagraphs 검증을 하게 되면
     *
     *        ○ ○ ○ ○ ○ ○ ○ ○  △ △ △ △ △ △ △ △
     *            ▲ ▲ ▲ ▲ ▲ ▲ ▲ ▲ ▲ ▲ ▲
     *                  ◇ ◇ ◇ ◇ ◇ ◇ ◇ ◇ ◇ ◇
     *
     * 와 같이 모두 분리된 Paragraph 로 인식 되므로
     * 이를 보정하여 하나의 Paragraph 로 클러스터링 한다.
     */
    private fun correctDetectAndSplitParagraphs(paragraphs: List<Paragraph>, writingDirection: WritingDirection): List<Paragraph> {
        val clustersVisited = mutableSetOf<Paragraph>()
        val clusters = mutableListOf<MutableList<Paragraph>>()

        fun expandCluster(paragraph: Paragraph, cluster: MutableList<Paragraph>) {
            val neighbors = paragraphs.filter {
                if (it != paragraph) {
                    Timber.tag(TAG)
                        .i("Correct cluster ${paragraph.isWriteDirectionOverlaps(it)}, ${paragraph.boundingBox}, ${paragraph.representation}, ${it.boundingBox}, ${it.representation}")
                }
                it != paragraph && paragraph.isWriteDirectionOverlaps(it)
            }
            cluster.add(paragraph)
            clustersVisited.add(paragraph)
            neighbors.forEach {
                if (!clustersVisited.contains(it)) {
                    expandCluster(it, cluster)
                }
            }
        }

        paragraphs.forEach { paragraph ->
            if (!clustersVisited.contains(paragraph)) {
                val cluster = mutableListOf<Paragraph>()
                expandCluster(paragraph, cluster)
                clusters.add(cluster)
            }
        }

        fun mergeParagraphs(paragraphs: List<Paragraph>): Paragraph {
            val allLines = paragraphs.flatMap { it.lines }
                .sortedWith(VisionSingleLineText.getComparator(writingDirection))
                .toMutableList()
            return Paragraph(allLines, writingDirection)
        }

        return clusters.map { cluster -> mergeParagraphs(cluster) }
    }

    /**
     * OCR 원문이 가로쓰기인지 세로쓰기인지 확인한다.
     */
    private fun detectVerticalWriting(text: Text): Boolean {
        var countGreaterThanOne = 0
        var countLessThanOne = 0

        for (textBlock in text.textBlocks) {
            for (line in textBlock.lines) {
                line.boundingBox?.let {
                    if (it.width() > it.height() && it.height() > 0) {
                        countGreaterThanOne++
                    } else {
                        countLessThanOne++
                    }
                }
            }
        }
        Timber.tag(TAG).i("isVerticalWriting  $countGreaterThanOne $countLessThanOne")
        return countGreaterThanOne < countLessThanOne
    }

    /**
     * todo 가로읽기 non-spacing 언어
     *      non-spacing 언어 의 경우 LINE_HORIZONTAL_DISTANCE_HEIGHT_RATIO_LIMIT 등의 조정이 필요하다.
     *      1. 중국어 LTR, non-spacing 중국어는 일반적으로 띄어쓰기를 사용하지 않습니다. 문자들이 연속적으로 쓰여지며, 구분은 주로 문장부호에 의존합니다.
     *      2. 일본어 LTR, non-spacing 일본어는 일반적으로 띄어쓰기를 사용하지 않습니다. 하지만 교육 자료나 어린이 책에서는 때때로 단어와 문법 요소를 구분하기 위해 띄어쓰기를 사용하기도 합니다.
     *      3. 태국어 LTR, non-spacing 태국어 문자는 연속적으로 쓰여지며, 문장의 끝을 나타내는 특정 기호를 사용합니다.
     */
    private fun setReferenceConstantValue(isVerticalWriting: Boolean, sourceLanguageCode: String) {
        val isNonSpacingLanguage = Language.isNonSpacingLanguage(sourceLanguageCode)
        Timber.tag(TAG)
            .d("setReferenceConstantValue - sourceLanguageCode: $sourceLanguageCode, isNonSpacingLanguage: $isNonSpacingLanguage, isVerticalWriting: $isVerticalWriting")

        WORD_AXIS_FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO = 0.85 // Word 행 중심축 유사판단 + 높이 유사판단 최소 유사율. (1에 가까울 수록 유사하다)
        WORD_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT = 0.63 // Word 동일 Line 판단 {요소 간 거리 : 요소 폰트높이 평균} 비율 한계비. (0에 가까울 수록 가깝다)
        LINE_FONT_HEIGHT_SIMILARITY_MINIMUM_RATIO = 0.68 // Line 폰트높이 유사판단 최소 유사율. (1에 가까울 수록 유사하다)
        LINE_WRITE_DIRECTION_OVERLAP_MINIMUM_RATIO = 0.84 // Line 동일 Paragraph 판단 텍스트 읽기 방향 최소 겹침 비율
        LINE_FONT_HEIGHT_COLOR_SPACING_AFFINITY_LIMIT = 0.62 // Line 폰트높이 유사성 x 색상 유사성 x {행간 : 요소 폰트높이 평균} 비 affinity 한계비
        LINE_AXIS_HEIGHT_SIMILARITY_MINIMUM_RATIO = 0.87 // Line 행 중심축 유사판단 + 폰트높이 유사판단 최소 유사율. (1에 가까울 수록 유사하다)
        LINE_WRITE_DIRECTION_DISTANCE_FONT_HEIGHT_RATIO_LIMIT = 0.91 // Line 동일 Line 판단 {요소 간 거리 : 요소 폰트높이 평균} 비율 한계비. (0에 가까울 수록 가깝다)

        if (isVerticalWriting) {
            if (isNonSpacingLanguage) {
                if (sourceLanguageCode == "ja") {
                    // todo 후리가나 처리
                }
            } else {

            }
        } else {
            if (isNonSpacingLanguage) {
                if (sourceLanguageCode == "ja") {
                    // todo 후리가나 처리
                }
            } else {

            }
        }
    }
}

fun Text.Element._toWord(writingDirection: WritingDirection): Word {
    val chars = this.symbols.map { symbol ->
        Char(symbol.boundingBox!!, symbol.text, writingDirection)
    }
    return Word(this.boundingBox!!, this.text, writingDirection, chars)
}

fun Text.Line._toLine(writingDirection: WritingDirection): Line {
    val words = this.elements.map { element ->
        element._toWord(writingDirection)
    }.toMutableList()
    return Line(words, writingDirection)
}



