package com.example.takstud.data.local

import com.example.takstud.model.Period
import com.example.takstud.model.Schedule

val defaultSchedules = listOf(
    Schedule(
        id = "6A",
        studentClass = "6°A",
        periodo = Period.MANHA,
        details = """ 
Segunda
1º (06:50-07:40): HIS / BRUNO
2º (07:40-08:30): HIS / BRUNO
3º (08:30-09:20): MAT / CRISTINA
4º (09:40-10:30): MAT / CRISTINA
5º (10:30-11:20): GEO / MARIA F
6º (11:20-12:10): GEO / MARIA F

Terça
1º (06:50-07:40): MAT / CRISTINA
2º (07:40-08:30): MAT / CRISTINA
3º (08:30-09:20): CIÊN / ILEIA
4º (09:40-10:30): CIÊN / ILEIA
5º (10:30-11:20): PORT / ROSANA
6º (11:20-12:10): PORT / ROSANA

Quarta
1º (06:50-07:40): CIÊN / ILEIA
2º (07:40-08:30): CIÊN / ILEIA
3º (08:30-09:20): MAT / CRISTINA
4º (09:40-10:30): MAT / CRISTINA
5º (10:30-11:20): ARTE / RENATA
6º (11:20-12:10): ARTE / RENATA

Quinta
1º (06:50-07:40): PORT / ROSANA
2º (07:40-08:30): PORT / ROSANA
3º (08:30-09:20): INGL / NEUSA
4º (09:40-10:30): INGL / NEUSA
5º (10:30-11:20): GEO / MARIA F
6º (11:20-12:10): GEO / MARIA F

Sexta
1º (06:50-07:40): PORT / ROSANA
2º (07:40-08:30): PORT / ROSANA
3º (08:30-09:20): HIS / BRUNO
4º (09:40-10:30): HIS / BRUNO
5º (10:30-11:20): ED.F / ANSELMO
6º (11:20-12:10): ED.F / ANSELMO
""".trimIndent()
    ),
    Schedule(
        id = "7A",
        studentClass = "7°A",
        periodo = Period.MANHA,
        details = """
Segunda
1º (06:50-07:40): MAT / CRISTINA
2º (07:40-08:30): MAT / CRISTINA
3º (08:30-09:20): GEO / MARIA F
4º (09:40-10:30): GEO / MARIA F
5º (10:30-11:20): CIÊN / LARA
6º (11:20-12:10): CIÊN / LARA

Terça
1º (06:50-07:40): GEO / MARIA F
2º (07:40-08:30): GEO / MARIA F
3º (08:30-09:20): PORT / SANDRA
4º (09:40-10:30): PORT / SANDRA
5º (10:30-11:20): MAT / CRISTINA
6º (11:20-12:10): MAT / CRISTINA

Quarta
1º (06:50-07:40): MAT / CRISTINA
2º (07:40-08:30): MAT / CRISTINA
3º (08:30-09:20): HIS / BRUNO
4º (09:40-10:30): HIS / BRUNO
5º (10:30-11:20): ED.F / ANSELMO
6º (11:20-12:10): ED.F / ANSELMO

Quinta
1º (06:50-07:40): PORT / SANDRA
2º (07:40-08:30): PORT / SANDRA
3º (08:30-09:20): ARTE / RENATA
4º (09:40-10:30): ARTE / RENATA
5º (10:30-11:20): HIS / BRUNO
6º (11:20-12:10): HIS / BRUNO

Sexta
1º (06:50-07:40): CIÊN / LARA
2º (07:40-08:30): CIÊN / LARA
3º (08:30-09:20): PORT / SANDRA
4º (09:40-10:30): PORT / SANDRA
5º (10:30-11:20): INGL / ANA
6º (11:20-12:10): INGL / ANA
""".trimIndent()
    ),
    Schedule(
        id = "7B",
        studentClass = "7°B",
        periodo = Period.MANHA,
        details = """
Segunda
1º (06:50-07:40): GEO / MARIA F
2º (07:40-08:30): GEO / MARIA F
3º (08:30-09:20): PORT / ROSANA
4º (09:40-10:30): PORT / ROSANA
5º (10:30-11:20): MAT / CRISTINA
6º (11:20-12:10): MAT / CRISTINA

Terça
1º (06:50-07:40): CIÊN / ILEIA
2º (07:40-08:30): CIÊN / ILEIA
3º (08:30-09:20): MAT / CRISTINA
4º (09:40-10:30): MAT / CRISTINA
5º (10:30-11:20): GEO / MARIA F
6º (11:20-12:10): GEO / MARIA F

Quarta
1º (06:50-07:40): ED.F / ANSELMO
2º (07:40-08:30): ED.F / ANSELMO
3º (08:30-09:20): CIÊN / ILEIA
4º (09:40-10:30): CIÊN / ILEIA
5º (10:30-11:20): MAT / CRISTINA
6º (11:20-12:10): MAT / CRISTINA

Quinta
1º (06:50-07:40): ARTE / RENATA
2º (07:40-08:30): ARTE / RENATA
3º (08:30-09:20): HIS / BRUNO
4º (09:40-10:30): HIS / BRUNO
5º (10:30-11:20): PORT / ROSANA
6º (11:20-12:10): PORT / ROSANA

Sexta
1º (06:50-07:40): HIS / BRUNO
2º (07:40-08:30): HIS / BRUNO
3º (08:30-09:20): INGL / ANA
4º (09:40-10:30): INGL / ANA
5º (10:30-11:20): PORT / ROSANA
6º (11:20-12:10): PORT / ROSANA
""".trimIndent()
    ),
    Schedule(
        id = "8A",
        studentClass = "8°A",
        periodo = Period.MANHA,
        details = """
Segunda
1º (06:50-07:40): PORT / EVA
2º (07:40-08:30): PORT / EVA
3º (08:30-09:20): MAT / ALE
4º (09:40-10:30): MAT / ALE
5º (10:30-11:20): HIS / BRUNO
6º (11:20-12:10): HIS / BRUNO

Terça
1º (06:50-07:40): PORT / EVA
2º (07:40-08:30): PORT / EVA
3º (08:30-09:20): ED.F / ANSELMO
4º (09:40-10:30): ED.F / ANSELMO
5º (10:30-11:20): CIÊN / ILEIA
6º (11:20-12:10): CIÊN / ILEIA

Quarta
1º (06:50-07:40): MAT / ALE
2º (07:40-08:30): MAT / ALE
3º (08:30-09:20): ARTE / MARCIA B
4º (09:40-10:30): ARTE / MARCIA B
5º (10:30-11:20): CIÊN / ILEIA
6º (11:20-12:10): CIÊN / ILEIA

Quinta
1º (06:50-07:40): HIS / BRUNO
2º (07:40-08:30): HIS / BRUNO
3º (08:30-09:20): GEO / MARIA F
4º (09:40-10:30): GEO / MARIA F
5º (10:30-11:20): PORT / EVA
6º (11:20-12:10): PORT / EVA

Sexta
1º (06:50-07:40): INGL / ANA C
2º (07:40-08:30): INGL / ANA C
3º (08:30-09:20): GEO / MARIA F
4º (09:40-10:30): GEO / MARIA F
5º (10:30-11:20): MAT / ALE
6º (11:20-12:10): MAT / ALE
""".trimIndent()
    ),
    Schedule(
        id = "9A",
        studentClass = "9°A",
        periodo = Period.MANHA,
        details = """
Segunda
1º (06:50-07:40): MAT / ALE
2º (07:40-08:30): MAT / ALE
3º (08:30-09:20): HIS / BRUNO
4º (09:40-10:30): HIS / BRUNO
5º (10:30-11:20): PORT / EVA
6º (11:20-12:10): PORT / EVA

Terça
1º (06:50-07:40): ED.F / ANSELMO
2º (07:40-08:30): ED.F / ANSELMO
3º (08:30-09:20): PORT / EVA
4º (09:40-10:30): PORT / EVA
5º (10:30-11:20): CIÊN / LARA
6º (11:20-12:10): CIÊN / LARA

Quarta
1º (06:50-07:40): ARTE / MARCIA B
2º (13:20-14:10): ARTE / MARCIA B
3º (08:30-09:20): MAT / ALE
4º (09:40-10:30): MAT / ALE
5º (10:30-11:20): HIS / BRUNO
6º (11:20-12:10): HIS / BRUNO

Quinta
1º (06:50-07:40): GEO / MARIA F
2º (07:40-08:30): GEO / MARIA F
3º (08:30-09:20): PORT / EVA
4º (09:40-10:30): PORT / EVA
5º (10:30-11:20): INGL / NEUSA
6º (11:20-12:10): INGL / NEUSA

Sexta
1º (06:50-07:40): MAT / ALE
2º (07:40-08:30): MAT / ALE
3º (08:30-09:20): CIÊN / LARA
4º (09:40-10:30): CIÊN / LARA
5º (10:30-11:20): GEO / MARIA F
6º (11:20-12:10): GEO / MARIA F
""".trimIndent()
    ),
    Schedule(
        id = "9B",
        studentClass = "9°B",
        periodo = Period.MANHA,
        details = """
Segunda
1º (06:50-07:40): PORT / ROSANA
2º (07:40-08:30): PORT / ROSANA
3º (08:30-09:20): CIÊN / LARA
4º (09:40-10:30): CIÊN / LARA
5º (10:30-11:20): MAT / ALE
6º (11:20-12:10): MAT / ALE

Terça
1º (06:50-07:40): HIS / MÁRCIA P
2º (07:40-08:30): HIS / MÁRCIA P
3º (08:30-09:20): GEO / MARIA F
4º (09:40-10:30): GEO / MARIA F
5º (10:30-11:20): ED.F / ANSELMO
6º (11:20-12:10): ED.F / ANSELMO

Quarta
1º (06:50-07:40): HIS / MÁRCIA P
2º (07:40-08:30): HIS / MÁRCIA P
3º (08:30-09:20): PORT / ROSANA
4º (09:40-10:30): PORT / ROSANA
5º (10:30-11:20): MAT / ALE
6º (11:20-12:10): MAT / ALE

Quinta
1º (06:50-07:40): INGL / NEUSA
2º (07:40-08:30): INGL / NEUSA
3º (08:30-09:20): PORT / ROSANA
4º (09:40-10:30): PORT / ROSANA
5º (10:30-11:20): ARTE / RENATA
6º (11:20-12:10): ARTE / RENATA

Sexta
1º (06:50-07:40): GEO / MARIA F
2º (07:40-08:30): GEO / MARIA F
3º (08:30-09:20): MAT / ALE
4º (09:40-10:30): MAT / ALE
5º (10:30-11:20): CIÊN / LARA
6º (11:20-12:10): CIÊN / LARA
""".trimIndent()
    ),
    Schedule(
        id = "6B-TARDE",
        studentClass = "6°B",
        periodo = Period.TARDE,
        details = """
Segunda
1º (12:30-13:20): HIS / MÁRCIA P
2º (13:20-14:10): HIS / MÁRCIA P
3º (14:10-15:00): CIÊN / ILEIA
4º (15:20-16:10): CIÊN / ILEIA
5º (16:10-17:00): GEO / JOÃO M
6º (17:00-17:50): GEO / JOÃO M

Terça
1º (12:30-13:20): PORT / SANDRA
2º (13:20-14:10): PORT / SANDRA
3º (14:10-15:00): MAT / QUINHA
4º (15:20-16:10): MAT / QUINHA
5º (16:10-17:00): INGL / NEUSA
6º (17:00-17:50): INGL / NEUSA

Quarta
1º (12:30-13:20): MAT / QUINHA
2º (13:20-14:10): MAT / QUINHA
3º (14:10-15:00): ED.F / ANSELMO
4º (15:20-16:10): ED.F / ANSELMO
5º (16:10-17:00): HIS / MÁRCIA P
6º (17:00-17:50): HIS / MÁRCIA P

Quinta
1º (12:30-13:20): GEO / JOÃO M
2º (13:20-14:10): GEO / JOÃO M
3º (14:10-15:00): ARTE / RENATA
4º (15:20-16:10): ARTE / RENATA
5º (16:10-17:00): PORT / SANDRA
6º (17:00-17:50): PORT / SANDRA

Sexta
1º (12:30-13:20): CIÊN / ILEIA
2º (13:20-14:10): CIÊN / ILEIA
3º (14:10-15:00): PORT / SANDRA
4º (15:20-16:10): PORT / SANDRA
5º (16:10-17:00): MAT / QUINHA
6º (17:00-17:50): MAT / QUINHA
""".trimIndent()
    ),
    Schedule(
        id = "6C-TARDE",
        studentClass = "6°C",
        periodo = Period.TARDE,
        details = """
Segunda
1º (12:30-13:20): CIÊN / ILEIA
2º (13:20-14:10): CIÊN / ILEIA
3º (14:10-15:00): INGL / NEUSA
4º (15:20-16:10): INGL / NEUSA
5º (16:10-17:00): HIS / MÁRCIA P
6º (17:00-17:50): HIS / MÁRCIA P

Terça
1º (12:30-13:20): GEO / JOÃO M
2º (13:20-14:10): GEO / JOÃO M
3º (14:10-15:00): PORT / SANDRA
4º (15:20-16:10): PORT / SANDRA
5º (16:10-17:00): HIS / MÁRCIA P
6º (17:00-17:50): HIS / MÁRCIA P

Quarta
1º (12:30-13:20): GEO / JOÃO M
2º (13:20-14:10): GEO / JOÃO M
3º (14:10-15:00): MAT / QUINHA
4º (15:20-16:10): MAT / QUINHA
5º (16:10-17:00): CIÊN / ILEIA
6º (17:00-17:50): CIÊN / ILEIA

Quinta
1º (12:30-13:20): ARTE / RENATA
2º (13:20-14:10): ARTE / RENATA
3º (14:10-15:00): PORT / SANDRA
4º (15:20-16:10): PORT / SANDRA
5º (16:10-17:00): MAT / QUINHA
6º (17:00-17:50): MAT / QUINHA

Sexta
1º (12:30-13:20): ED.F / ANSELMO
2º (13:20-14:10): ED.F / ANSELMO
3º (14:10-15:00): MAT / QUINHA
4º (15:20-16:10): MAT / QUINHA
5º (16:10-17:00): PORT / SANDRA
6º (17:00-17:50): PORT / SANDRA
""".trimIndent()
    ),
    Schedule(
        id = "7C-TARDE",
        studentClass = "7°C",
        periodo = Period.TARDE,
        details = """
Segunda
1º (12:30-13:20): MAT / CLAUDIA
2º (13:20-14:10): MAT / CLAUDIA
3º (14:10-15:00): GEO / JOÃO M
4º (15:20-16:10): GEO / JOÃO M
5º (16:10-17:00): ARTE / RENATA
6º (17:00-17:50): ARTE / RENATA

Terça
1º (12:30-13:20): CIÊN / LARA
2º (13:20-14:10): CIÊN / LARA
3º (14:10-15:00): MAT / CLAUDIA
4º (15:20-16:10): MAT / CLAUDIA
5º (16:10-17:00): PORT / MURILO
6º (17:00-17:50): PORT / MURILO

Quarta
1º (12:30-13:20): HIS / MÁRCIA P
2º (13:20-14:10): HIS / MÁRCIA P
3º (14:10-15:00): MAT / CLAUDIA
4º (15:20-16:10): MAT / CLAUDIA
5º (16:10-17:00): ED.F / ANSELMO
6º (17:00-17:50): ED.F / ANSELMO

Quinta
1º (12:30-13:20): PORT / MURILO
2º (13:20-14:10): PORT / MURILO
3º (14:10-15:00): HIS / MÁRCIA P
4º (15:20-16:10): HIS / MÁRCIA P
5º (16:10-17:00): GEO / JOÃO M
6º (17:00-17:50): GEO / JOÃO M

Sexta
1º (12:30-13:20): INGL / ANA C
2º (13:20-14:10): INGL / ANA C
3º (14:10-15:00): CIÊN / LARA
4º (15:20-16:10): CIÊN / LARA
5º (16:10-17:00): PORT / MURILO
6º (17:00-17:50): PORT / MURILO
""".trimIndent()
    ),
    Schedule(
        id = "8B-TARDE",
        studentClass = "8°B",
        periodo = Period.TARDE,
        details = """
Segunda
1º (12:30-13:20): MAT / QUINHA
2º (13:20-14:10): MAT / QUINHA
3º (14:10-15:00): PORT / EVA
4º (15:20-16:10): PORT / EVA
5º (16:10-17:00): INGL / NEUSA
6º (17:00-17:50): INGL / NEUSA

Terça
1º (12:30-13:20): PORT / EVA
2º (13:20-14:10): PORT / EVA
3º (14:10-15:00): HIS / MÁRCIA P
4º (15:20-16:10): HIS / MÁRCIA P
5º (16:10-17:00): GEO / JOÃO M
6º (17:00-17:50): GEO / JOÃO M

Quarta
1º (12:30-13:20): CIÊN / ILEIA
2º (13:20-14:10): CIÊN / ILEIA
3º (14:10-15:00): GEO / JOÃO M
4º (15:20-16:10): GEO / JOÃO M
5º (16:10-17:00): MAT / QUINHA
6º (17:00-17:50): MAT / QUINHA

Quinta
1º (12:30-13:20): HIS / MÁRCIA P
2º (13:20-14:10): HIS / MÁRCIA P
3º (14:10-15:00): PORT / EVA
4º (15:20-16:10): PORT / EVA
5º (16:10-17:00): ARTE / RENATA
6º (17:00-17:50): ARTE / RENATA

Sexta
1º (12:30-13:20): MAT / QUINHA
2º (13:20-14:10): MAT / QUINHA
3º (14:10-15:00): ED.F / ANSELMO
4º (15:20-16:10): ED.F / ANSELMO
5º (16:10-17:00): CIÊN / ILEIA
6º (17:00-17:50): CIÊN / ILEIA
""".trimIndent()
    ),
    Schedule(
        id = "8C-TARDE",
        studentClass = "8°C",
        periodo = Period.TARDE,
        details = """
Segunda
1º (12:30-13:20): GEO / MARIA F
2º (13:20-14:10): GEO / MARIA F
3º (14:10-15:00): MAT / CLAUDIA
4º (15:20-16:10): MAT / CLAUDIA
5º (16:10-17:00): CIÊN / ILEIA
6º (17:00-17:50): CIÊN / ILEIA

Terça
1º (12:30-13:20): GEO / MARIA F
2º (13:20-14:10): GEO / MARIA F
3º (14:10-15:00): PORT / MURILO
4º (15:20-16:10): PORT / MURILO
5º (16:10-17:00): MAT / CLAUDIA
6º (17:00-17:50): MAT / CLAUDIA

Quarta
1º (12:30-13:20): MAT / CLAUDIA
2º (13:20-14:10): MAT / CLAUDIA
3º (14:10-15:00): HIS / MÁRCIA P
4º (15:20-16:10): HIS / MÁRCIA P
5º (16:10-17:00): ARTE / RENATA
6º (17:00-17:50): ARTE / RENATA

Quinta
1º (12:30-13:20): INGL / ANA C
2º (13:20-14:10): INGL / ANA C
3º (14:10-15:00): PORT / MURILO
4º (15:20-16:10): PORT / MURILO
5º (16:10-17:00): HIS / MÁRCIA P
6º (17:00-17:50): HIS / MÁRCIA P

Sexta
1º (12:30-13:20): PORT / MURILO
2º (13:20-14:10): PORT / MURILO
3º (14:10-15:00): CIÊN / ILEIA
4º (15:20-16:10): CIÊN / ILEIA
5º (16:10-17:00): ED.F / ANSELMO
6º (17:00-17:50): ED.F / ANSELMO
""".trimIndent()
    ),
    Schedule(
        id = "9C-TARDE",
        studentClass = "9°C",
        periodo = Period.TARDE,
        details = """
Segunda
1º (12:30-13:20): GEO / JOÃO M
2º (13:20-14:10): GEO / JOÃO M
3º (14:10-15:00): HIS / MÁRCIA P
4º (15:20-16:10): HIS / MÁRCIA P
5º (16:10-17:00): PORT / MURILO
6º (17:00-17:50): PORT / MURILO

Terça
1º (12:30-13:20): HIS / MÁRCIA P
2º (13:20-14:10): HIS / MÁRCIA P
3º (14:10-15:00): INGL / NEUSA
4º (15:20-16:10): INGL / NEUSA
5º (16:10-17:00): CIÊN / LARA
6º (17:00-17:50): CIÊN / LARA

Quarta
1º (12:30-13:20): ED.F / ANSELMO
2º (13:20-14:10): ED.F / ANSELMO
3º (14:10-15:00): ARTE / RENATA
4º (15:20-16:10): ARTE / RENATA
5º (16:10-17:00): MAT / CLAUDIA
6º (17:00-17:50): MAT / CLAUDIA

Quinta
1º (12:30-13:20): MAT / CLAUDIA
2º (13:20-14:10): MAT / CLAUDIA
3º (14:10-15:00): GEO / JOÃO M
4º (15:20-16:10): GEO / JOÃO M
5º (16:10-17:00): PORT / MURILO
6º (17:00-17:50): PORT / MURILO

Sexta
1º (12:30-13:20): MAT / CLAUDIA
2º (13:20-14:10): MAT / CLAUDIA
3º (14:10-15:00): PORT / MURILO
4º (15:20-16:10): PORT / MURILO
5º (16:10-17:00): CIÊN / LARA
6º (17:00-17:50): CIÊN / LARA
""".trimIndent()
    ),
    Schedule(
        id = "EJA",
        studentClass = "EJA",
        periodo = Period.EJA,
        details = """
Segunda
1º (18:50-19:35): INGL / NEUSA
2º (19:35-20:20): INGL / NEUSA
3º (20:20-21:05): PORT / SANDRA
4º (21:15-22:00): PORT / SANDRA
5º (22:00-22:45): PORT / SANDRA

Terça
1º (18:50-19:35): CIÊN / LARA
2º (19:35-20:20): ARTE / RENATA
3º (20:20-21:05): ARTE / RENata
4º (21:15-22:00): PORT / SANDRA
5º (22:00-22:45): PORT / SANDRA

Quarta
1º (18:50-19:35): VAGO
2º (19:35-20:20): GEO / JOÃO M
3º (20:20-21:05): GEO / JOÃO M
4º (21:15-22:00): MAT / QUINHA
5º (22:00-22:45): MAT / QUINHA

Quinta
1º (18:50-19:35): GEO / JOÃO M
2º (19:35-20:20): PORT / SANDRA
3º (20:20-21:05): MAT / QUINHA
4º (21:15-22:00): MAT / QUINHA
5º (22:00-22:45): MAT / QUINHA

Sexta
1º (18:50-19:35): CIÊN / LARA
2º (19:35-20:20): CIÊN / LARA
3º (20:20-21:05): HIS / BRUNO
4º (21:15-22:00): HIS / BRUNO
5º (22:00-22:45): HIS / BRUNO
""".trimIndent()
    )
)
