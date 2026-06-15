# Design System: Freezer-to-Feast

**ID:** `assets/20ef803394744152b399e39fe8c60802`

## Brand & Style
The design system embodies "Quiet Luxury"—an aesthetic defined by restraint, intentionality, and high-end craftsmanship. It targets an affluent audience that values wellness as a refined lifestyle rather than a chore. The UI evokes the atmosphere of a bespoke kitchen journal or a personal nutrition concierge.

The style is **Minimalist with Tactile Sophistication**. It prioritizes vast amounts of whitespace to signify breathing room and premium quality. Unlike standard SaaS platforms, this design system avoids aggressive call-to-actions, instead utilizing subtle tonal shifts, exquisite typography, and soft, organic shapes to guide the user through their culinary inventory and nourishment journey.

## Colors
The palette is rooted in a "Gourmet" aesthetic, moving away from high-vibrancy "app greens" toward more organic, muted tones found in high-end culinary environments.

- **Primary (Gourmet Green):** A deep, desaturated olive (`#3E4631` / `#28301c`) used for core branding, primary buttons, and authoritative text. It represents growth, stability, and premium ingredients.
- **Secondary (Champagne Yellow):** A soft, buttery cream (`#F9F4E0` / `#e7e3cf`) used for highlighted containers and subtle accents. It adds a warmth reminiscent of candlelight or fine silk.
- **Surface (Off-White):** A bone-toned neutral (`#FAF9F6`) serves as the primary canvas, providing a softer, more sophisticated look than pure white.
- **Muted Green:** A mid-tone olive (`#8C927D` / `#c3c9b2`) used for secondary icons and supporting graphical elements to maintain a monochromatic harmony.

## Typography
The typography system uses a classic pairing of a high-contrast serif and a modern geometric sans-serif to create a "Editorial" feel.

- **Headlines:** Playfair Display is utilized for all major headings. It should be typeset with slightly tighter letter-spacing for large sizes to emphasize its elegant strokes. Avoid all-caps for serifs to maintain a literary, inviting tone.
- **Body & UI:** Plus Jakarta Sans provides a clean, airy contrast. It is chosen for its generous x-height and friendly, open apertures, ensuring high legibility even in data-heavy tracking views.
- **Labels:** Small labels and metadata use uppercase Plus Jakarta Sans with increased tracking (letter-spacing) to denote a sense of "archival" organization, similar to high-end food packaging.

### Typography Specifications
*   **display-lg**: Playfair Display, 48px, SemiBold (600), Line Height: 1.1, Letter Spacing: -0.02em
*   **headline-lg**: Playfair Display, 32px, Medium (500), Line Height: 1.2
*   **headline-lg-mobile**: Playfair Display, 28px, Medium (500), Line Height: 1.2
*   **headline-md**: Playfair Display, 24px, Medium (500), Line Height: 1.3
*   **body-lg**: Plus Jakarta Sans, 18px, Regular (400), Line Height: 1.6, Letter Spacing: 0.01em
*   **body-md**: Plus Jakarta Sans, 16px, Regular (400), Line Height: 1.6
*   **label-md**: Plus Jakarta Sans, 14px, SemiBold (600), Line Height: 1.2, Letter Spacing: 0.05em
*   **label-sm**: Plus Jakarta Sans, 12px, Medium (500), Line Height: 1.2, Letter Spacing: 0.03em

## Layout & Spacing
The layout follows a **Fluid Grid** with an emphasis on "negative space as a feature." 

- **Grid:** A 12-column grid is used for desktop with wide 64px outer margins to "float" the content in the center of the screen, mimicking the margins of a luxury magazine.
- **Rhythm:** We use an 8px base unit. Vertical spacing between sections is intentionally generous (80px+) to prevent the interface from feeling cluttered or "utility-first."
- **Mobile:** Content reflows to a single column with 24px side margins. Cards and containers should span the full width minus margins to maximize touch targets for ingredient tracking.

### Spacing Scale
- `unit`: 8px
- `element-gap`: 16px
- `gutter`: 24px
- `container-padding-mobile`: 24px
- `container-padding-desktop`: 64px
- `section-gap`: 80px

## Elevation & Depth
In alignment with the Quiet Luxury theme, depth is achieved through **Tonal Layering** and **Ambient Shadows** rather than heavy borders or dark drop-shadows.

- **The Primary Layer:** Surfaces use the Off-White base.
- **The Raised Layer:** Cards and modals use the Champagne Yellow or a slightly lighter Off-White, elevated by an "Ultra-Ambient" shadow: a very large blur radius (32px-64px) with extremely low opacity (3-5%) tinted with the Gourmet Green. This creates a soft "lift" that feels natural and airy.
- **Borders:** When borders are necessary for structural clarity, use 1px lines in a very faint version of the Primary Green (10-15% opacity).

## Shapes
The shape language is "Generous and Organic." 

- **Standard Radius:** All buttons and input fields use a 12px (0.75rem) radius to feel soft yet structured (`ROUND_EIGHT`).
- **Large Components:** Main content cards and feature highlights use a 24px (1.5rem) radius (`rounded-xl`).
- **Icons:** Icons should feature rounded terminals and a consistent 1.5pt to 2pt stroke weight to match the elegance of the Plus Jakarta Sans typeface.

## Components
- **Buttons:** Primary buttons are filled with Gourmet Green (`#3E4631`) with Champagne Yellow text. Secondary buttons are "Ghost" style with a 1px Gourmet Green border. Transitions should be slow and ease-in-out to mimic a premium feel.
- **Cards:** Cards should have no visible border by default; instead, they rely on the tonal shift (Champagne Yellow surface) or the ambient shadow to define their boundaries.
- **Input Fields:** Use "Floating Label" inputs to keep the UI clean. The active state is indicated by a subtle color shift in the background rather than a bold border.
- **Chips/Badges:** For food categories (e.g., "Organic," "Frozen," "Poultry"), use small, pill-shaped badges with a low-opacity fill of the Gourmet Green and dark green text.
- **Lists:** Ingredient lists should have generous vertical padding (20px+) between items, separated by a hairline 1px divider that does not reach the full width of the container.
- **Progress Indicators:** Use thin, elegant lines for calorie or nourishment tracking, avoiding bulky progress bars. Use the Gourmet Green for the progress fill and Champagne Yellow for the track.
