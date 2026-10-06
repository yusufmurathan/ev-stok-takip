# Ev Stok

**Türkçe** | [English](README.en.md)

Evdeki sabit sarf malzemelerini (gıda, temizlik, hijyen) takip eden, tükenenleri tek
dokunuşla market listesine çeviren ve alışveriş sonrası tekrar stoğa çeken **tamamen
yerel çalışan** Android uygulaması.

- **Kotlin + Jetpack Compose** (Material 3)
- **Room** ile cihaz üzerinde SQLite kalıcılık
- **AMOLED koyu tema** (saf siyah arka plan)
- İnternet izni yok, hesap yok, senkronizasyon yok — tek kullanıcı, sıfır sürtünme

## Ekran Görüntüleri

| Stok | Market Listesi | Katalog |
|---|---|---|
| ![Stok](docs/screenshots/stock.png) | ![Market Listesi](docs/screenshots/market.png) | ![Katalog](docs/screenshots/catalog.png) |

## Çalışma Mantığı

### Durum odaklı takip (adet yok)
Ürünlerin gramı/rulosu sayılmaz. Her ürün üç durumdan birinde:

| Durum | Anlamı |
|---|---|
| **VAR** | Stokta, sorun yok |
| **AZ** | Az kaldı (tampon bitmek üzere) |
| **BİTTİ** | Tükendi, market listesinde |

Stok listesinde bir ürüne dokunmak durumu sırayla değiştirir:
`VAR → AZ → BİTTİ → VAR`. Basılı tutunca düzenleme/silme sayfası açılır.

### Tampon stok mantığı
Büyük ambalajdan küçük kabın (dispenser, sabunluk vb.) aktarılan ürünlerde ara kaplar
takip edilmez. Litrelik ana ambalaj çöpe gittiği an ürüne **BİTTİ** verilir; sabunluktaki
tampon, markete gidene kadar idare eder. **AZ** durumu bu tamponun eridiği erken
uyarıdır.

### Otomatik market listesi
BİTTİ/AZ işaretlenen ürünler "Market" sekmesinde kendiliğinden listelenir:
- Ürünü markette sepete atınca satırdaki kutucuğa dokun → ürün tekrar **VAR** olur ve
  listeden kaybolur (yanlışlıkla işaretlediysen "GERİ AL" ile geri alabilirsin).
- "Tümünü Alındı İşaretle" ile tek hamlede sıfırlama (check & reset).
- Alt menüdeki kırmızı rozet, listede bekleyen ürün sayısını gösterir.

### Ön tanımlı katalog
~90 hazır ev ürünü (peynir, salça, sıvı sabun, tuvalet kağıdı…) ilk açılışta tohumlanır.
Katalog ekranından tek dokunuşla stoğa eklenir; tekrar tekrar isim yazılmaz. Kendi
ürünlerin de (+ butonu) kataloğa kalıcı olarak eklenir.

## Ekranlar

1. **Stok** — ev envanteri; arama, kategori ve durum filtreleri, özet istatistikler.
2. **Market** — otomatik alışveriş listesi (BİTTİ + AZ KALDI bölümleri), geri alma
   desteği.
3. **Katalog** — hazır ürün havuzu + özel ürün tanımlama.

## Derleme

Gereksinimler: JDK 17+, Android SDK (compileSdk 35).

```bash
./gradlew assembleDebug        # debug APK → app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # birim testleri
```

Ya da projeyi Android Studio'da açıp çalıştırın.

## Mimari

```
app/src/main/java/com/evstok/app/
├── EvStokApp.kt            # Application + bağımlılık kabı (manuel DI)
├── MainActivity.kt         # Tek activity, bottom-bar navigasyon
├── data/                   # Room: entity/DAO/veritabanı, seed katalog, repository
└── ui/
    ├── theme/              # AMOLED renk şeması (saf siyah)
    ├── components/         # StatusChip, filtre satırları, ortak parçalar
    ├── dialogs/            # ürün ekle/düzenle, onay, alt sayfa
    ├── home/               # Stok ekranı + ViewModel
    ├── shopping/           # Market listesi ekranı + ViewModel
    └── catalog/            # Katalog ekranı + ViewModel
```

- MVVM: her ekran kendi `ViewModel`'ine sahip, UI Room `Flow`'ları üzerinden
  reaktif besleniyor; veritabanı tek doğruluk kaynağı.
- `catalog` tablosu ürün havuzunu, `stock` tablosu kullanıcının takip ettiği
  ürünleri tutar (kategori bazlı Türkçe alfabetik sıralama `Collator` ile).

## Lisans

[MIT](LICENSE)
