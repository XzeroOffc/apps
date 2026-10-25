# SONORA Music Tools for Android

Aplikasi Android native dengan splash loading, login pemilik, dashboard, navigasi antarroute, dan halaman Cari Lagu, Pemutar Musik, Koleksi, serta Playlist yang sementara bertanda **Coming Soon**.

## Akun demo

- Username: `owner`
- Password: `SonoraOwner#2026`

Kredensial demo berada di `app/src/main/java/com/sonora/musictools/OwnerCredentials.kt`. Login masih lokal di dalam aplikasi. Jangan bagikan APK dengan password demo. Untuk penggunaan sungguhan, pindahkan autentikasi ke backend.

## Membuat APK lewat GitHub Actions

1. Upload isi folder project ini ke repository GitHub.
2. Pastikan branch utama bernama `main`.
3. Buka tab **Actions** dan jalankan workflow **Build Android APK**, atau push perubahan ke `main`.
4. Setelah workflow selesai, buka hasil run dan unduh artifact `sonora-debug-apk`.

Workflow membangun APK debug. Runner GitHub hanya membangun file APK, bukan meng-host aplikasi untuk dibuka terus-menerus.

## Build lokal

Gunakan JDK 17, Android SDK API 36, dan Gradle 8.13. Dari folder project:

```bash
gradle assembleDebug
```

APK akan dibuat di `app/build/outputs/apk/debug/app-debug.apk`.

## Catatan

Source ini menyiapkan UI dan navigasi. Pencarian lagu, pemutaran audio, koleksi, dan playlist belum terhubung ke layanan musik.
