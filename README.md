# MARIANAS AI — Android App

Native Android app (Kotlin + Jetpack Compose) by Leonore Tech Team.

## Cara Build

1. Buka folder `marianas-android` ini di Android Studio (File → Open)
2. Tunggu Gradle sync selesai. Kalau Android Studio menyarankan update versi
   AGP/Kotlin/library lain, ikuti saja — versi yang ditulis di sini valid per
   pertengahan 2026 tapi mungkin sudah ada yang lebih baru.
3. Run ke device/emulator (▶️ di toolbar, pilih device, tunggu build selesai)

## Login

App ini punya gate login lokal sederhana (BUKAN sistem auth sungguhan):
- Username: `Dragonic`
- Password: `Leonore`

Login diminta ulang setiap kali app dibuka (tidak ada "remember me").
Catatan: kredensial ini ada di kode sumber sebagai plain text — siapa pun
yang membongkar (decompile) APK bisa membacanya. Ini cukup untuk pemakaian
pribadi, tapi bukan proteksi keamanan sungguhan kalau APK ini disebar ke orang lain.

## Provider AI yang didukung

- **OpenRouter** — banyak model gratis (akhiran `:free`)
- **Groq** — inference cepat (LPU hardware), model id isi manual
- **Google AI (Gemini native)** — pakai header `x-goog-api-key`, bukan Bearer token
- **Custom 1/2/3** — 3 slot bebas, masing-masing bisa pilih format OpenAI-style
  atau Gemini-style, untuk endpoint AI apapun yang kamu punya

Fallback otomatis bisa diaktifkan: kalau provider yang aktif gagal, app
otomatis coba provider lain yang sudah dikonfigurasi.

## Penyimpanan

100% lokal di perangkat lewat Room database. Tidak ada cloud sync, backend,
atau backend pihak ketiga apa pun. Foto yang diupload disimpan di
penyimpanan privat app (`filesDir/chat_images`).

## Known caveats (hal yang belum sempat diverifikasi lewat compile asli)

Project ini ditulis tanpa Android SDK/Gradle yang bisa benar-benar compile di
lingkungan pembuatannya — jadi ada beberapa titik yang **mungkin** perlu
sedikit penyesuaian versi library saat build pertama kali:

- `androidx.compose.foundation.layout.FlowRow` di `ProviderDashboard.kt`
  (API yang relatif baru — kalau "unresolved reference", ganti ke `Row` manual)
- Versi BOM Compose / AGP / Kotlin / KSP di `build.gradle.kts` — kalau Android
  Studio menyarankan upgrade, ikuti saja, breaking change biasanya minim

## Font

Saat ini pakai `FontFamily.Monospace` bawaan Android sebagai pengganti
JetBrains Mono / Orbitron (font asli versi web). Untuk pakai font asli:
1. Download file `.ttf` JetBrains Mono & Orbitron
2. Taruh di `app/src/main/res/font/`
3. Edit `ui/theme/Type.kt`, ganti `FontFamily.Monospace` dengan
   `FontFamily(Font(R.font.nama_file, FontWeight.Normal))`

## Icon

Launcher icon dibuat dari logo Leonore Tech Team yang diberikan, sudah
di-generate ke semua density (mdpi–xxxhdpi) + adaptive icon untuk Android 8+.
