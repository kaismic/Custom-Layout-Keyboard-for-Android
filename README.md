# Custom Layout Keyboard for Android

## Previews:
<div style="display: flex">
    <img src="images/screenshot_0.png" style="width: 30%; height: 30%; padding:0% 1%">
    <img src="images/screenshot_1.png" style="width: 30%; height: 30%; padding:0% 1%">
    <img src="images/screenshot_2.png" style="width: 30%; height: 30%; padding:0% 1%">
</div>

## Layout Inheritance
<img src="images/layout_inheritance_diagram.png">

Anyone can modify and use this app for personal use.

No commercial use.

I built this app because there were no good android keyboards which allowed me to use a custom key layout of my own.

Default layout is based on the Colemak layout but slightly different.

Created based on Microsoft SwiftKey.

Implements most of the basic functionality of Microsoft SwiftKey such as swipe on spacebar to change language, swipe on the letters to delete by word and long click to input subtexts.

To change the letter key layout, change the strings in `letterList` in `EnglishLayout.kt` or `KoreanLayout.kt`.

To change the special key layout, change the strings in `subTextLetterList` in `MainKeyboardService.kt`.

Current Supported Languages:
- English
- Korean

Requires Android 13 (API level 33) or higher

Tested Devices:
- Google Pixel 4a
