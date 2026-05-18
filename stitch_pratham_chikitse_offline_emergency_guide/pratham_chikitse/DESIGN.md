---
name: Pratham-Chikitse
colors:
  surface: '#f7f9ff'
  surface-dim: '#cfdbea'
  surface-bright: '#f7f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#ecf4ff'
  surface-container: '#e3effe'
  surface-container-high: '#dde9f9'
  surface-container-highest: '#d7e4f3'
  on-surface: '#111d27'
  on-surface-variant: '#59413d'
  inverse-surface: '#26323d'
  inverse-on-surface: '#e7f2ff'
  outline: '#8d706c'
  outline-variant: '#e1bfb9'
  surface-tint: '#b02d21'
  primary: '#9e2016'
  on-primary: '#ffffff'
  primary-container: '#c0392b'
  on-primary-container: '#ffe5e1'
  inverse-primary: '#ffb4a9'
  secondary: '#006d37'
  on-secondary: '#ffffff'
  secondary-container: '#7bf8a1'
  on-secondary-container: '#007239'
  tertiary: '#754800'
  on-tertiary: '#ffffff'
  tertiary-container: '#965d00'
  on-tertiary-container: '#ffe7cf'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad5'
  primary-fixed-dim: '#ffb4a9'
  on-primary-fixed: '#410000'
  on-primary-fixed-variant: '#8e130c'
  secondary-fixed: '#7efba4'
  secondary-fixed-dim: '#61de8a'
  on-secondary-fixed: '#00210c'
  on-secondary-fixed-variant: '#005228'
  tertiary-fixed: '#ffddb9'
  tertiary-fixed-dim: '#ffb961'
  on-tertiary-fixed: '#2b1700'
  on-tertiary-fixed-variant: '#663e00'
  background: '#f7f9ff'
  on-background: '#111d27'
  surface-variant: '#d7e4f3'
typography:
  headline-lg:
    fontFamily: Noto Sans Kannada
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Noto Sans Kannada
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  body-lg:
    fontFamily: Noto Sans Kannada
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 26px
  body-md:
    fontFamily: Noto Sans Kannada
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-lg:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 20px
    letterSpacing: 0.5px
  emergency-num:
    fontFamily: Roboto
    fontSize: 32px
    fontWeight: '900'
    lineHeight: 40px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  margin-mobile: 16px
  gutter-mobile: 12px
  tap-target-min: 48px
  tile-size: 80px
---

## Brand & Style

The brand personality is authoritative, calm, and immediate. This design system is built for users in high-stress, "panic-mode" situations where cognitive load must be minimized. The visual direction follows a **High-Contrast, Action-Oriented** style. It prioritizes utility over aesthetics, utilizing bold color blocks and universally recognizable iconography to guide users through life-saving procedures without requiring extensive literacy or technical proficiency.

The design system employs a "Safety-First" aesthetic: 
- **Urgency through Color:** Functional use of color to signal the severity of medical conditions.
- **Icon-Driven Navigation:** Relying on glyphs to bridge language barriers and speed up recognition.
- **Tactile Reliability:** Elements feel physical and substantial, giving users confidence in their interactions even with shaky hands.

## Colors

The palette is strictly functional, mapping directly to triage and safety states. 

- **Emergency Red (#C0392B):** Used for critical life-threatening actions (Bleeding, Heart Attack, Choking) and the primary 'Call 108' trigger.
- **Safe Green (#27AE60):** Used for recovery positions, "situation controlled" signals, and successful completion of steps.
- **Warning Amber (#F39C12):** Used for non-life-threatening but urgent issues (Minor burns, fractures) and cautionary instructions.
- **Dark Navy (#1C2833):** The primary color for text and icons to ensure maximum contrast against the white surface.
- **Surface White (#FDFEFE):** A clean, high-reflectance base to ensure legibility in bright outdoor sunlight common in rural environments.

Color should never be the *only* indicator of meaning; it must always be paired with an icon or clear text label.

## Typography

This design system utilizes **Noto Sans Kannada** as the primary typeface to ensure local language support is legible even on low-resolution displays. **Roboto** is used as a secondary typeface specifically for numerical data and technical labels, as its glyphs are highly distinct for rapid scanning.

- **Legibility First:** Line heights are generous to prevent Kannada script stack-up (vattu symbols) from overlapping.
- **Hierarchical Scale:** Headlines are significantly larger than body text to allow users to "skim" during a crisis.
- **Numerical Emphasis:** Phone numbers and countdown timers use the `emergency-num` style for maximum visibility.

## Layout & Spacing

The layout uses a **Fluid Grid** system optimized for one-handed thumb use. 

- **Safe Margins:** A minimum 16px margin on the left and right edges ensures content isn't obscured by phone cases or gripped fingers.
- **Vertical Rhythm:** A strict 8px baseline grid is used. All vertical spacing between elements must be a multiple of 8px.
- **The "Panic" Zone:** The bottom 30% of the screen is reserved for the most critical actions (Call 108, Audio Support) as this is the easiest area to reach during physical movement or stress.
- **Instructional Flow:** Card-based instructions follow a single-column vertical stack to prevent confusion about the "next step."

## Elevation & Depth

This design system avoids complex shadows or realistic textures that could distract the user. Depth is conveyed through **High-Contrast Outlines** and **Tonal Layers**.

- **Level 0 (Surface):** The background (#FDFEFE).
- **Level 1 (Cards/Tiles):** Subtle 1px borders in Dark Navy or the category's semantic color. 
- **Level 2 (Active Elements):** Buttons and active tiles use a "pressed" state indicated by a solid color fill, rather than a shadow change.
- **FAB Elevation:** The Audio FAB uses a soft, pulsing outer glow (using the Secondary Green) to draw attention without obstructing the content beneath it.

## Shapes

The shape language is **Rounded**, striking a balance between the clinical feel of a hospital and the approachability of a community tool.

- **Container Radius:** Standard cards and tiles use a 0.5rem (8px) corner radius.
- **Interactive Radius:** Buttons use a slightly larger 1rem (16px) radius to make them appear more "pressable."
- **Emergency Tiles:** These remain square with a subtle 8px radius to maximize the internal surface area for large icons.

## Components

### Emergency Tiles
The core navigation element. Min 80x80dp. Each tile must contain a large central icon and a clear Kannada label underneath. Tiles are color-coded by medical severity.

### Instruction Cards
High-contrast cards that contain one clear instruction per screen. Features a large "Next" button and a visual illustration or icon. Step numbers (e.g., "1/5") must be clearly visible in the top right.

### Persistent 'Call 108' Bar
A fixed bottom bar in Emergency Red (#C0392B). It spans the full width of the screen. The text "CALL 108" and the phone icon must be white for maximum contrast.

### Pulsing Audio FAB
A circular button (min 56x56dp) with a speaker icon. It features a persistent pulse animation to signify that voice-guided instructions are available in Kannada.

### Large Buttons
All buttons have a minimum height of 56dp. Primary action buttons use solid fills; secondary actions use thick 2px borders with the same color.

### Checkboxes & Radios
Oversized tap targets (min 48x48dp) with heavy strokes to ensure they are easily toggled during shaky-hand scenarios.