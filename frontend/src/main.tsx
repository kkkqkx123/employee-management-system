import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

// Initialize security features
import { initializeSecurity, validateSecuritySetup } from './security'

// Initialize security on app startup
initializeSecurity()
validateSecuritySetup()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
