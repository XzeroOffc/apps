# SONORA Music Tools for Android

Aplikasi Android native dengan splash loading, login pemilik, dashboard, pencarian lagu, pemutar audio di dalam aplikasi, lirik, dan navigasi koleksi serta playlist.

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

## Search, pemutar audio, dan lirik

1. Buka menu **Cari** dan masukkan API key Sylvatica saat diminta.
2. Key disimpan terenkripsi menggunakan Android Keystore dan tidak ditanam di source code.
3. Cari lagu berdasarkan judul atau artis, lalu pilih hasil untuk membuka halaman pemutar dan lirik.
4. SONORA mencari URL audio lewat endpoint `/api/search/play` dan memutarnya langsung di aplikasi. Tombol putar/jeda dan penggeser posisi tersedia di halaman pemutar.

Pencarian memakai endpoint `/api/search/spotify`, lirik memakai `/api/search/lyrics`, dan audio memakai `/api/search/play`. Semua permintaan memakai API key melalui header `X-API-Key`.

## Build lokal

Gunakan JDK 17, Android SDK API 36, dan Gradle 8.13. Dari folder project:

```bash
gradle assembleDebug
```

APK akan dibuat di `app/build/outputs/apk/debug/app-debug.apk`.

## Catatan

Search, pemutar audio, dan lirik terhubung ke endpoint Sylvatica. Koleksi dan playlist masih berupa halaman Coming Soon.
