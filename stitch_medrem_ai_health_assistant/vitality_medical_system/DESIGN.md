---
name: Vitality Medical System
colors:
  surface: '#f8faf5'
  surface-dim: '#d8dbd6'
  surface-bright: '#f8faf5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4ef'
  surface-container: '#ecefea'
  surface-container-high: '#e7e9e4'
  surface-container-highest: '#e1e3de'
  on-surface: '#191c1a'
  on-surface-variant: '#3e4946'
  inverse-surface: '#2e312e'
  inverse-on-surface: '#eff1ec'
  outline: '#6e7976'
  outline-variant: '#bec9c5'
  surface-tint: '#006b5e'
  primary: '#005147'
  on-primary: '#ffffff'
  primary-container: '#006b5e'
  on-primary-container: '#95e8d8'
  inverse-primary: '#83d5c5'
  secondary: '#4a635f'
  on-secondary: '#ffffff'
  secondary-container: '#cae5e0'
  on-secondary-container: '#4e6763'
  tertiary: '#643f00'
  on-tertiary: '#ffffff'
  tertiary-container: '#845500'
  on-tertiary-container: '#ffd29a'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#9ff2e1'
  primary-fixed-dim: '#83d5c5'
  on-primary-fixed: '#00201b'
  on-primary-fixed-variant: '#005046'
  secondary-fixed: '#cde8e3'
  secondary-fixed-dim: '#b1ccc7'
  on-secondary-fixed: '#061f1d'
  on-secondary-fixed-variant: '#334b48'
  tertiary-fixed: '#ffddb5'
  tertiary-fixed-dim: '#ffb957'
  on-tertiary-fixed: '#2a1800'
  on-tertiary-fixed-variant: '#643f00'
  background: '#f8faf5'
  on-background: '#191c1a'
  surface-variant: '#e1e3de'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '400'
    lineHeight: 36px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 34px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  margin-mobile: 16px
  margin-tablet: 24px
  gutter: 16px
  touch-target: 48px
---

## Brand & Style

This design system is built on a **Corporate / Modern** aesthetic, specifically tailored for the healthcare sector. It adheres to Material Design 3 principles to ensure familiarity and ease of use for a wide demographic, including those who may be elderly or have low digital literacy.

The personality is **Trustworthy, Professional, and Supportive**. This is achieved through generous whitespace, a stabilizing color palette, and high-legibility typography. The UI avoids unnecessary decorative elements, focusing instead on clarity and reducing the cognitive load of medication management. Emotional resonance is achieved through "soft" precision—using rounded corners and gentle shadows to make medical technology feel approachable rather than clinical or cold.

## Colors

The palette is anchored by a **Medical Teal (#006B5E)**, derived from the reference imagery to evoke health, hygiene, and reliability. 

- **Primary:** Used for key actions (FABs, primary buttons) and active states.
- **Secondary:** A muted slate-teal used for less prominent UI elements and supporting icons.
- **Surface & Background:** A soft "off-white" (#FBFDF8) is used to reduce screen glare, which is critical for users who may have sensitive vision or are checking the app frequently.
- **Semantic Colors:** Green for doses taken, Amber for pending/upcoming, and Red for missed doses or critical alerts. High contrast ratios (minimum 4.5:1) are strictly maintained for all text on background combinations.

## Typography

**Inter** is utilized as the singular font family to maintain a systematic, utilitarian feel. The hierarchy prioritizes medical data (dosage amounts, medication names) through weight variations rather than color alone. 

Large-scale headlines are used for dashboard summaries, while `title-md` and `body-lg` serve as the workhorses for medication lists and instructions. To aid accessibility, line heights are slightly increased to ensure text does not appear cramped, facilitating easier reading for users with visual impairments.

## Layout & Spacing

This design system follows a **fluid grid** model based on a 4dp/8dp baseline. 

- **Mobile:** A 4-column grid with 16dp side margins. 
- **Tablet:** An 8-column grid with 24dp side margins.
- **Desktop:** A 12-column grid with a maximum content width of 1200dp, centered on screen.

Interactive elements strictly adhere to a **48dp minimum touch target** to ensure users with limited dexterity can navigate the interface without error. Spacing between cards and list items is kept generous (12dp-16dp) to prevent accidental taps.

## Elevation & Depth

Visual hierarchy is established using **Tonal Layers** and **Ambient Shadows** as defined in the Material 3 specification.

- **Level 0:** The main background surface (Neutral #FBFDF8).
- **Level 1:** Cards and sheets that are "resting." Use a very subtle, diffused shadow (4dp blur, 5% opacity) and a slight color tint to differentiate from the background.
- **Level 2:** Elements that are being interacted with or dragged.
- **Level 3+:** Modal Bottom Sheets and Dialogs. These use a 16dp elevation and a 32% black scrim over the background to focus the user's attention on the critical medical task at hand.

Shadows are never pure black; they are tinted with the primary or secondary color to maintain a cohesive, soft medical aesthetic.

## Shapes

The shape language is **Rounded**, mirroring the friendly and supportive brand personality. 

- **Standard Containers:** Cards, Input Fields, and Modal Bottom Sheets use a 16dp - 24dp corner radius.
- **Small Elements:** Buttons and Chips use a fully rounded (pill-shaped) style to distinguish them as highly interactive.
- **Status Indicators:** Progress rings use a rounded stroke-cap to feel less aggressive than sharp-edged gauges.

## Components

### Buttons & FABs
Primary actions use the **Floating Action Button (FAB)** in the bottom right, utilizing the Primary Teal. Regular buttons are pill-shaped with 16dp horizontal padding.

### Cards
Medication reminders are housed in **Elevated Cards**. They feature a vertical color strip on the left edge corresponding to the medication's status (e.g., Green for "Taken").

### Modal Bottom Sheets
Used for adding new medications or viewing dose details. This keeps the user in context without navigating away from the dashboard. Top corners are rounded at 28dp.

### Progress Rings
The "Daily Adherence" ring uses a thick 8dp stroke. The background track is a low-opacity version of the primary color, while the active track uses the solid Primary Teal.

### Input Fields
Outlined style with a 16dp corner radius. Labels are always visible to prevent memory-related errors. Error states use a 2dp red border and supporting text.

### Bottom Navigation
Uses Material 3 icons with active states indicated by a pill-shaped container around the icon. Labels are persistent to ensure navigation is always clear.