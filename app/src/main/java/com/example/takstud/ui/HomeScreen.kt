package com.example.takstud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite

/**
 *  HomeScreen - Tela inicial do TakStud com design Profissional
 * - SOU PROFESSOR: Leva ao login de professor (código de acesso)
 * - SOU ALUNO/RESPONSÁVEL: Leva ao login de aluno/responsável (RA)
 *
 * @param onProfessorClick Callback quando clica em "SOU PROFESSOR"
 * @param onAlunoClick Callback quando clica em "SOU ALUNO/RESPONSÁVEL"
 */
@Composable
fun HomeScreen(
    onProfessorClick: () -> Unit,
    onAlunoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        Scaffold(
            containerColor = PureWhite,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ✨ Logo/Titulo
                Text(
                    "✨ TAKSTUD ✨",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 52.sp
                    ),
                    color = NavyBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Descrição
                Text(
                    "Sistema de Gestão Acadêmica",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 👨‍🏫 Botão Professor
                Button(
                    onClick = onProfessorClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "👨‍🏫 SOU PROFESSOR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Texto divisor
                Text(
                    "━━━━ OU ━━━━",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGray.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 👨‍👩‍👧 Botão Aluno/Responsável
                Button(
                    onClick = onAlunoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "👨‍👩‍👧 SOU ALUNO/RESPONSÁVEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Rodapé com versão
                Text(
                    "v2.0 - PROFISSIONAL 🎓",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGray.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
