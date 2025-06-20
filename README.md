# Android Liquid Glass

An Android app that simulates Apple's Liquid Glass effect, Android 13 and above is required.

![](./artworks/features.jpg)

## About shaders

You mustn't use the shaders in the project for commercial purposes, they are only for educational purposes.

## Glass parameters

|         Parameter         | Availability | Verification |
|:-------------------------:|--------------|--------------|
|  Inner refraction height  | ✔️           | ✔️           |
|  Inner refraction amount  | ✔️           | ✔️           |
|  Outer refraction height  | ❌            | ❌            |
|  Outer refraction amount  | ❌            | ❌            |
|     Dispersion height     | 🚧           | ❌            |
|       Bleed opacity       | ✔️           | 🚧           |
| Eccentric factor (add-on) | ✔️           | ✔️           |

## Comparisons

Android device: Google Pixel 4 XL (the smallest width is adjusted to 440 dp to match the density of the iOS device)

iOS device: iPhone 16 Pro Max (simulator)

Test glass area size: 300 x 300, corner radius: 30

|                        iOS                        |                        Android                        |
|:-------------------------------------------------:|:-----------------------------------------------------:|
| ![](./artworks/inner_refraction/ios/-60%2020.png) | ![](./artworks/inner_refraction/android/-60%2020.png) |

Complete comparisons:

- [Inner refraction](docs/Inner%20refraction%20comparisons.md)
- [Bleed](docs/Bleed%20comparisons.md)

## Special thanks

- [GlassExplorer](https://github.com/ktiays/GlassExplorer)
