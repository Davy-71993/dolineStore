package com.example.doline.views.components



import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.LetterSpacing
import com.example.doline.ui.theme.displayFontFamily

@Composable
fun AppText (
    text: String,
    modifier: Modifier = Modifier,
    variant: TextType = TextType.Body,
    color: Color = MaterialTheme.colorScheme.onBackground,
    letterSpacing: TextUnit = LetterSpacing.NORMAL,
    maxLines: Int = 100,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign = TextAlign.Start,
    style: TextStyle = TextStyle()
)  {

    var fontSize = FontSize.SM
    var fontWeight = FontWeight.Normal


    when (variant){
        TextType.ExtraSMall -> {
            fontSize = FontSize.XXS
        }
        TextType.Small -> {
            fontSize = FontSize.XS
        }
        TextType.Body -> {}

        TextType.Label -> {
            fontSize = FontSize.MD
            fontWeight = FontWeight.Bold
        }

        TextType.LabelSmall -> {
            fontSize = FontSize.XS
            fontWeight = FontWeight.Bold
        }

        TextType.Heading -> {
            fontSize = FontSize.XL
            fontWeight = FontWeight.W800
        }

        TextType.Brand -> {
            fontSize = FontSize.BRAND
            fontWeight = FontWeight.W900
        }
    }


    Text(
        text,
        modifier = modifier,
        fontSize = fontSize,
        color = color,
        fontFamily = displayFontFamily,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        overflow = textOverflow,
        textAlign = textAlign,
        style = style
    )
}

enum class TextType {
    ExtraSMall,
    Small, // Small text
    Body, // Default text
    Label, // Labels
    LabelSmall, // Small labels
    Heading, // Heading
    Brand // Big text
}