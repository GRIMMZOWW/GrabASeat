# GrabASeat Design System (MASTER.md)

## 1. Aesthetic & Direction
- **Atmosphere**: Warm dark cinema experience. Deep charcoal and obsidian surfaces inspired by theatre auditorium lighting, with a single rich amber accent (`#e5a93c`).
- **Principles**:
  - No gradients (solid matte surfaces only).
  - No cards inside cards.
  - No stock posters, placeholder images, or empty image boxes. Typographic cards celebrate the film title, language, genre, and duration directly.
  - Icons are inline SVG only (no emojis, no icon fonts).
  - WCAG AA text contrast (minimum 4.5:1 on all surfaces).
  - Clear, visible keyboard focus indicators (`2px solid #e5a93c` with `2px` offset).
  - Responsive at 375px, 768px, 1024px, and 1440px breakpoints.
  - Respects `@media (prefers-reduced-motion: reduce)`.

---

## 2. Typography (Google Fonts)
- **Display Font (Movie Titles, Brand & Page Headings)**: `Cinzel`, serif (weights: 600, 700).
- **Body Font (Navigation, Form Inputs, Badges, Metadata, Buttons)**: `Plus Jakarta Sans`, sans-serif (weights: 400, 500, 600).

```css
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@600;700&family=Plus+Jakarta+Sans:wght@400;500;600&display=swap');
```

### Scale
- **Display / H1**: `2rem` (32px) desktop / `1.625rem` (26px) mobile, `line-height: 1.2`, font-family: `'Cinzel', serif`.
- **H2 / Card Title**: `1.375rem` (22px), `line-height: 1.3`, font-family: `'Cinzel', serif`.
- **H3**: `1.125rem` (18px), `line-height: 1.4`, font-family: `'Plus Jakarta Sans', sans-serif`, weight: 600.
- **Body Regular**: `0.9375rem` (15px), `line-height: 1.5`, font-family: `'Plus Jakarta Sans', sans-serif`.
- **Small / Metadata / Badges**: `0.8125rem` (13px), `line-height: 1.4`, font-family: `'Plus Jakarta Sans', sans-serif`.

---

## 3. Color Palette
- **Background Root**: `#111215` (Deep auditorium black)
- **Surface Layer 1 (Header, Cards, Panels)**: `#1a1c22`
- **Surface Layer 2 (Input fields, hover states)**: `#242730`
- **Border / Divider**: `#2e323e`
- **Accent (Single accent color)**: `#e5a93c` (Warm amber gold)
- **Accent Hover**: `#cca43b`
- **Accent Contrast Text**: `#111215` (High contrast text on amber buttons)
- **Text Primary**: `#f3f4f6` (Off-white, 14.5:1 contrast against `#111215`)
- **Text Secondary / Muted**: `#9ca3af` (Muted gray, 5.2:1 contrast against `#111215`)
- **Danger / Error (Forms & server errors)**: `#f87171`
- **Danger Surface**: `#2d1818`

---

## 4. Spacing Scale
- `--space-1`: `4px`
- `--space-2`: `8px`
- `--space-3`: `12px`
- `--space-4`: `16px`
- `--space-5`: `20px`
- `--space-6`: `24px`
- `--space-8`: `32px`
- `--space-10`: `40px`
- `--space-12`: `48px`
- `--space-16`: `64px`

---

## 5. Radius Scale
- `--radius-sm`: `4px` (Tags, small pills)
- `--radius-md`: `6px` (Inputs, buttons)
- `--radius-lg`: `8px` (Movie cards, modal dialogs, forms)

---

## 6. Shadows (Solid, Subtle, Non-diffused)
- `--shadow-card`: `0 2px 8px rgba(0, 0, 0, 0.4)`
- `--shadow-card-hover`: `0 4px 14px rgba(0, 0, 0, 0.6)`
- `--shadow-dropdown`: `0 6px 16px rgba(0, 0, 0, 0.7)`

---

## 7. Component Styling Specifications

### Buttons
- **Primary Button**:
  - Background: `#e5a93c`
  - Text: `#111215`, font-weight: `600`, font-size: `0.9375rem`
  - Padding: `10px 18px`
  - Border: `none`
  - Border radius: `6px`
  - Hover: background `#cca43b`, cursor `pointer`
  - Disabled: background `#4b432f`, text `#8b7c5e`, cursor `not-allowed`
- **Secondary / Ghost Button**:
  - Background: `transparent`
  - Text: `#f3f4f6`
  - Padding: `8px 14px`
  - Border: `1px solid #2e323e`
  - Border radius: `6px`
  - Hover: background `#242730`, border-color `#9ca3af`

### Form Inputs
- Background: `#1a1c22`
- Border: `1px solid #2e323e`
- Border radius: `6px`
- Text: `#f3f4f6`, font-size: `0.9375rem`
- Padding: `10px 12px`
- Label: font-size `0.875rem`, font-weight `500`, color `#f3f4f6`, display block, margin-bottom `6px`
- Focus: outline `none`, border-color `#e5a93c`, box-shadow `0 0 0 2px rgba(229, 169, 60, 0.2)`
- Inline Error: color `#f87171`, font-size `0.8125rem`, margin-top `6px`

### Typographic Movie Cards
- Background: `#1a1c22`
- Border: `1px solid #2e323e`
- Border radius: `8px`
- Padding: `24px`
- Title: `'Cinzel', serif`, `1.375rem`, weight `700`, color `#f3f4f6`, margin-bottom `12px`
- Badges (Language & Genre): background `#242730`, border `1px solid #2e323e`, color `#e5a93c`, font-size `0.75rem`, padding `3px 8px`, border-radius `4px`, normal case, no letter-spacing, font-weight `500`
- Duration: inline SVG clock icon + text, color `#9ca3af`, font-size `0.8125rem`

### Accessibility & Motion
- All interactive elements possess explicit `:focus-visible` styles with `outline: 2px solid #e5a93c; outline-offset: 2px;`.
- Transitions use standard `0.15s ease-in-out`.
- Under `@media (prefers-reduced-motion: reduce)`, transitions and animations are set to `none`.
