module.exports = {
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: '#090B0F', // Primary Background
        surface: '#11151D',    // Surface Color
        card: '#171D28',       // Surface Light Color
        divider: '#222B3A',    // Border Color
        accent: {
          blue: '#38BDF8',     // Accent Blue
          emerald: '#10B981',  // Neon Emerald (Success)
          amber: '#F59E0B',    // Crisp Amber (Warning)
          red: '#EF4444',      // Accent Red (Error)
        },
        text: {
          primary: '#F8FAFC',
          secondary: '#94A3B8',
          muted: '#64748B',
        }
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        glass: '0 8px 32px 0 rgba(0, 0, 0, 0.37)',
      }
    },
  },
  plugins: [],
}
