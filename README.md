# Android Liquid Glass

An Android app that simulates Apple's Liquid Glass effect, Android 13 and above is required.

Download the app [here](./app/release/app-release.apk).

![](./artworks/features.jpg)

## About shaders

You mustn't use the shaders in the project for commercial purposes, they are only for educational purposes.

## Library

The library is under construction, use on your own risk.

It doesn't support nested glass effects for now.

### Examples

```kotlin
val providerState = rememberLiquidGlassProviderState()

CompositionLocalProvider(
    LocalLiquidGlassProviderState provides providerState
) {
    Box(
        Modifier
            .liquidGlassProvider(providerState)
    )

    Box(
        Modifier
            .liquidGlass(
                LiquidGlassStyle(
                    CornerShape.large,
                    innerRefraction = InnerRefraction(
                        height = RefractionValue(8.dp),
                        amount = RefractionValue.Full
                    ),
                    material = GlassMaterial(
                        blurRadius = 8.dp,
                        whitePoint = 0.1f,
                        chromaMultiplier = 1.5f
                    )
                )
            )
    )
}
```

## Glass parameters

|        Parameter        | Availability | Verification |
|:-----------------------:|--------------|--------------|
| Inner refraction height | ✔️           | ✔️           |
| Inner refraction amount | ✔️           | ✔️           |
| Outer refraction height | ❌            | ❌            |
| Outer refraction amount | ❌            | ❌            |
|      Bleed amount       | ✔️           | ❌            |
|    Bleed blur radius    | ✔️           | ❌            |
|      Bleed opacity      | ✔️           | 🚧           |
|       Blur radius       | ✔️           | ❌            |

Other extensions:

- Border color, width, angle, decay
- Contrast, white point, chroma multiplier
- Eccentric factor
- Dispersion height (🚧)

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

## Star history

[![Star history chart](https://api.star-history.com/svg?repos=Kyant0/AndroidLiquidGlass&type=Date)](https://www.star-history.com/#Kyant0/AndroidLiquidGlass&Date)
