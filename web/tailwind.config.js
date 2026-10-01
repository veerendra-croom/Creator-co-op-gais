/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "#090B0F",
        surface: "#11151D",
        surfaceLight: "#171D28",
        divider: "#222B3A",
        accentBlue: "#38BDF8",
        neonEmerald: "#10B981",
        accentRed: "#EF4444",
        crispAmber: "#F59E0B",
        textPrimary: "#FFFFFF",
        textSecondary: "#94A3B8",
        textMuted: "#64748B"
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['Fira Code', 'Courier New', 'monospace'],
      },
      boxShadow: {
        glass: '0 8px 32px 0 rgba(0, 0, 0, 0.37)',
      }
    },
  },
  plugins: [],
}
