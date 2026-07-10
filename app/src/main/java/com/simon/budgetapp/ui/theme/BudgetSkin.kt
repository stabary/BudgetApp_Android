package com.simon.budgetapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.simon.budgetapp.data.AppSkin

/**
 * Palette de personnalisation pour BudgetDetailScreen.
 * Un champ à null signifie "garder le style Material par défaut".
 */
data class BudgetDetailPalette(
    val screenBackground: Color? = null,
    val topBarBackground: Color? = null,
    val topBarContentColor: Color? = null,
    val monthlyCardBackground: Color? = null,
    val monthlyCardContentColor: Color? = null,
    val accountCardBackground: Color? = null,
    val accountCardContentColor: Color? = null,
    val upcomingCardBackground: Color? = null,
    val upcomingCardContentColor: Color? = null,
    val transactionRowBackground: Color? = null,
    val transactionRowContentColor: Color? = null,
    val ringSliceColors: List<Color>? = null,
    val historyBarPrimaryColor: Color? = null,
    val historyBarSecondaryColor: Color? = null,
    val fabBackground: Color? = null,
    val fabContentColor: Color? = null,
    val useRoundedRingChart: Boolean = false,
    val showCloudDecoration: Boolean = false
)

fun paletteFor(skin: AppSkin): BudgetDetailPalette = when (skin) {
    AppSkin.CLASSIQUE -> BudgetDetailPalette()

    AppSkin.DOUCEUR -> BudgetDetailPalette(
        screenBackground = Color(0xFFE4FFF7),
        topBarBackground = Color(0xFF181818),
        topBarContentColor = Color(0xFFC5ACD0),
        monthlyCardBackground = Color(0xFF42949A),
        monthlyCardContentColor = Color(0xFF16302F),
        accountCardBackground = Color(0xFF82C1C8),
        accountCardContentColor = Color(0xFF16302F),
        upcomingCardBackground = Color(0xFF42949A),
        upcomingCardContentColor = Color(0xFF16302F),
        transactionRowBackground = Color(0xFF82C1C8),
        transactionRowContentColor = Color(0xFF16302F),
        ringSliceColors = listOf(
            Color(0xFFA9D9C8), // Frais appartement
            Color(0xFF80749B), // Courses
            Color(0xFF4E4270), // Alimentation
            Color(0xFF2B4E57), // Assurance
            Color(0xFF265B58)  // Livret LEP
        ),
        historyBarPrimaryColor = Color(0xFF4E4270),
        historyBarSecondaryColor = Color(0xFFC5ACD2),
        fabBackground = Color(0xFF42949A),
        fabContentColor = Color.White,
        useRoundedRingChart = true,
        showCloudDecoration = true
    )
}

fun skinDisplayName(skin: AppSkin): String = when (skin) {
    AppSkin.CLASSIQUE -> "Classique"
    AppSkin.DOUCEUR -> "Douceur"
}