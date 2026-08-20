/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Source Sans 3', 'Segoe UI', 'sans-serif'],
      },
      colors: {
        brand: {
          50: '#eef4fb',
          100: '#d5e4f4',
          500: '#1d4f91',
          700: '#163a6b',
          900: '#0d2444',
        },
      },
    },
  },
  plugins: [],
}
