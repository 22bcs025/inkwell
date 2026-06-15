/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        serif: ['"Instrument Serif"', 'Georgia', 'serif'],
      },
      typography: {
        DEFAULT: {
          css: {
            fontFamily: 'Inter, system-ui, sans-serif',
            lineHeight: '1.8',
            maxWidth: 'none',
            color: '#1e293b',
            'h1,h2,h3,h4': { fontFamily: '"Instrument Serif", Georgia, serif' },
            a: { color: '#6366f1' },
          },
        },
      },
    },
  },
  plugins: [require('@tailwindcss/typography')],
};
