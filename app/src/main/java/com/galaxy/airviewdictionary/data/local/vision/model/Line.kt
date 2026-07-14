package com.galaxy.airviewdictionary.data.local.vision.model

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import android.graphics.Rect
import com.galaxy.airviewdictionary.data.local.vision.WritingDirection
import com.galaxy.airviewdictionary.extensions._cutDecimal
import com.galaxy.airviewdictionary.extensions._unionWith
import timber.log.Timber
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.properties.Delegates


/**
 * 한 줄 짜리 텍스트
 */
data class Line(
    val words: MutableList<Word>,
    override val writingDirection: WritingDirection
) : VisionSingleLineText {

    private var boundingBoxCache: Rect? = null
    private var wordsHashCodeCache: Int? = null

    override val boundingBox: Rect
        get() {
            val currentWordsHashCode = words.hashCode()
            if (boundingBoxCache == null || wordsHashCodeCache != currentWordsHashCode) {
                boundingBoxCache = if (words.isEmpty()) {
                    Rect()
                } else {
                    words.map { it.boundingBox }.reduce { acc, rect -> acc._unionWith(rect) }
                }
                wordsHashCodeCache = currentWordsHashCode
            }
            return boundingBoxCache!!
        }

    override val representation: String
        get() = words.joinToString(separator = " ") { it.representation }

    /**
     * Line 에 [Word] 를 삽입
     */
    fun addWord(newWord: Word) {
        var left = 0
        var right = words.size

        when (writingDirection) {
            WritingDirection.LTR -> { // LTR and default case
                while (left < right) {
                    val mid = (left + right) / 2
                    if (words[mid].boundingBox.left < newWord.boundingBox.left) {
                        left = mid + 1
                    } else {
                        right = mid
                    }
                }
            }

            WritingDirection.RTL -> {
                while (left < right) {
                    val mid = (left + right) / 2
                    if (words[mid].boundingBox.left > newWord.boundingBox.left) {
                        left = mid + 1
                    } else {
                        right = mid
                    }
                }
            }

            else -> { // TTB_LTR, TTB_RTL
                while (left < right) {
                    val mid = (left + right) / 2
                    if (words[mid].boundingBox.top < newWord.boundingBox.top) {
                        left = mid + 1
                    } else {
                        right = mid
                    }
                }
            }

        }
        // 삽입 위치에 새로운 Word를 추가
        words.add(left, newWord)
    }

    var fontColor by Delegates.notNull<Int>()

    var backgroundColor by Delegates.notNull<Int>()

    /**
     * 폰트 색상과 백그라운드 색상을 알아냄
     */
    fun setFontAndBackgroundColors(bitmap: Bitmap) {
        // 1. line의 왼쪽, 오른쪽, 위쪽, 아래쪽 모든 방향에서 픽셀을 2개씩 추출
        val yCenter = boundingBox.centerY()
        val xCenter = boundingBox.centerX()

        val left = boundingBox.left - 2
        val right = boundingBox.right + 2
        val top = boundingBox.top - 2
        val bottom = boundingBox.bottom + 2

        val x1 = (boundingBox.left + boundingBox.width() / 3).coerceIn(0, bitmap.width - 1)
        val x2 = (boundingBox.left + 2 * boundingBox.width() / 3).coerceIn(0, bitmap.width - 1)
        val y1 = (boundingBox.top + boundingBox.height() / 3).coerceIn(0, bitmap.height - 1)
        val y2 = (boundingBox.top + 2 * boundingBox.height() / 3).coerceIn(0, bitmap.height - 1)

        val leftPixels = listOfNotNull(
            if (left >= 0) bitmap[left, y1] else null,
            if (left >= 0) bitmap[left, y2] else null
        )

        val rightPixels = listOfNotNull(
            if (right < bitmap.width) bitmap[right, y1] else null,
            if (right < bitmap.width) bitmap[right, y2] else null
        )

        val topPixels = listOfNotNull(
            if (top >= 0) bitmap[x1, top] else null,
            if (top >= 0) bitmap[x2, top] else null
        )

        val bottomPixels = listOfNotNull(
            if (bottom < bitmap.height) bitmap[x1, bottom] else null,
            if (bottom < bitmap.height) bitmap[x2, bottom] else null
        )

        val pixelColors = leftPixels + rightPixels + topPixels + bottomPixels

        // 추출한 8개의 픽셀에서 가장 많은 픽셀을 차지하는 색상을 backgroundColor로 설정
        val backgroundColor = pixelColors.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: Color.WHITE

        // 2. line에서 글자 하나 선택 (특수문자가 아닌 첫 번째 Char를 찾고, 없다면 가운데 Char를 선택)
        val allChars = words.flatMap { it.chars }
        val midIndex = allChars.size / 2
        val nonSpecialChar = (0..midIndex).firstNotNullOfOrNull { index ->
            listOf(allChars.getOrNull(midIndex + index), allChars.getOrNull(midIndex - index)).find {
                it?.representation?.any { ch -> ch.isLetterOrDigit() } == true
            }
        } ?: allChars[midIndex]


        // 3. bitmap에서 선택된 Char의 boundingBox에 해당하는 픽셀을 추출
        val charBoundingBox = nonSpecialChar.boundingBox
        Timber.tag("Line").d("charBoundingBox $nonSpecialChar ${nonSpecialChar.boundingBox}")

        // 가로 방향 1/3 지점, 2/3 지점에서 두 줄의 픽셀 데이터를 추출
        val horizontalPixelData1 = IntArray(charBoundingBox.width())
        val horizontalY1 = (charBoundingBox.top + charBoundingBox.height() / 3).coerceIn(0, bitmap.height - 1)
        bitmap.getPixels(horizontalPixelData1, 0, charBoundingBox.width(), max(charBoundingBox.left, 0), horizontalY1, charBoundingBox.width(), 1)

        val horizontalPixelData2 = IntArray(charBoundingBox.width())
        val horizontalY2 = (charBoundingBox.top + 2 * charBoundingBox.height() / 3).coerceIn(0, bitmap.height - 1)
        bitmap.getPixels(horizontalPixelData2, 0, charBoundingBox.width(), charBoundingBox.left, horizontalY2, charBoundingBox.width(), 1)

        // 세로 방향 1/3 지점, 2/3 지점에서 두 줄의 픽셀 데이터를 추출
        val verticalPixelData1 = IntArray(charBoundingBox.height())
        val verticalX1 = (charBoundingBox.left + charBoundingBox.width() / 3).coerceIn(0, bitmap.width - 1)
        bitmap.getPixels(verticalPixelData1, 0, 1, verticalX1, charBoundingBox.top, 1, charBoundingBox.height())

        val verticalPixelData2 = IntArray(charBoundingBox.height())
        val verticalX2 = (charBoundingBox.left + 2 * charBoundingBox.width() / 3).coerceIn(0, bitmap.width - 1)
        bitmap.getPixels(verticalPixelData2, 0, 1, verticalX2, charBoundingBox.top, 1, charBoundingBox.height())

        val charPixelData = horizontalPixelData1 + horizontalPixelData2 + verticalPixelData1 + verticalPixelData2

        // 4. 추출된 픽셀에서 backgroundColor와 유사도가 가장 낮은 색상을 fontColor로 설정
        val colorCountMap = mutableMapOf<Int, Int>()
        for (pixel in charPixelData) {
            colorCountMap[pixel] = colorCountMap.getOrDefault(pixel, 0) + 1
        }

        val sortedColors = colorCountMap.entries.sortedByDescending { it.value }

        var fontColor = Color.BLACK
        var leastSimilarity = Double.MAX_VALUE

        sortedColors.forEach { (color, _) ->
            val similarity = calculateColorSimilarity(backgroundColor, color)
            if (similarity < leastSimilarity) {
                leastSimilarity = similarity
                fontColor = color
            }
        }

        // charPixelData 빈도순으로 포맷된 색상값과 빈도수 형태로 출력
        val colorFrequency = sortedColors.joinToString(", ") { entry ->
            "${String.format(Locale.US, "#%08X", entry.key)} - ${entry.value}"
        }

        val pixelColorsString = pixelColors.joinToString(", ") { entry ->
            "${String.format(Locale.US, "#%08X", entry)}"
        }

        Timber.tag("Line").d(
            "-------------- getFontAndBackgroundColors " +
                    "fontColor: ${String.format("#%08X", fontColor)}, " +
                    "backgroundColor: ${String.format("#%08X", backgroundColor)}, " +
                    "line: ${representation}, " +
                    "nonSpecialChar: ${nonSpecialChar.representation}, " +
                    "pixelColorsString: $pixelColorsString, " +
                    "charPixelData: $colorFrequency"
        )

        this.fontColor = fontColor
        this.backgroundColor = backgroundColor
    }

    fun getColorSimilarity(other: Line): Double {

        Timber.tag("VisionRepository").i(
            "----- getColorSimilarity ${String.format("#%08X", fontColor)} "
                    + "${String.format("#%08X", backgroundColor)} "
                    + "${String.format("#%08X", other.fontColor)} "
                    + "${String.format("#%08X", other.backgroundColor)} "
        )

        val returnThis = if (other.fontColor == fontColor) {
            if (other.backgroundColor == backgroundColor) {
                1.0
            } else {
                calculateColorSimilarity(other.backgroundColor, backgroundColor)
            }
        } else if (other.backgroundColor == backgroundColor) {
            calculateColorSimilarity(other.fontColor, fontColor)
        } else {
            val fontColorSimilarity = calculateColorSimilarity(other.fontColor, fontColor)
            val backgroundColorSimilarity = calculateColorSimilarity(other.backgroundColor, backgroundColor)
            (fontColorSimilarity + backgroundColorSimilarity) / 2
        }


        return returnThis
    }

    /**
     * 두 색상값의 유사도를 계산
     */
    private fun calculateColorSimilarity(color1: Int, color2: Int): Double {
        val red1 = Color.red(color1)
        val green1 = Color.green(color1)
        val blue1 = Color.blue(color1)

        val red2 = Color.red(color2)
        val green2 = Color.green(color2)
        val blue2 = Color.blue(color2)

        // 각 색상값의 차이를 구하고 백분위 환산용 상수(0.39)를 곱함
        val redDifference = abs(red1 - red2) * 0.39
        val greenDifference = abs(green1 - green2) * 0.39
        val blueDifference = abs(blue1 - blue2) * 0.39

        // 유사율을 계산
        val redSimilarity = (100 - redDifference) / 100
        val greenSimilarity = (100 - greenDifference) / 100
        val blueSimilarity = (100 - blueDifference) / 100

        Timber.tag("VisionRepository").d(
            "----- calculateColorSimilarity ${String.format("#%08X", color1)} $red1 $green1 $blue1 ${
                String.format(
                    "#%08X",
                    color2
                )
            } $red2 $green2 $blue2 -- ${redSimilarity._cutDecimal()}  ${greenSimilarity._cutDecimal()}  ${blueSimilarity._cutDecimal()}  "
        )

        // 평균 유사율 계산
        return (redSimilarity + greenSimilarity + blueSimilarity) / 3
    }

}
