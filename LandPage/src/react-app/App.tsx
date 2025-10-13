import { BrowserRouter as Router, Routes, Route } from "react-router";
import { lazy, Suspense, useEffect } from "react";

// Lazy load route components
const HomePage = lazy(() => import("@/react-app/pages/Home"));
const FAQPage = lazy(() => import("@/react-app/pages/FAQ"));
const PrivacyPolicyPage = lazy(() => import("@/react-app/pages/PrivacyPolicy"));
const TermsPage = lazy(() => import("@/react-app/pages/Terms"));
const OAuthRedirect = lazy(() => import("@/react-app/pages/OAuthRedirect"));

// Redirect component for PDF
const PDFRedirect = () => {
  useEffect(() => {
    window.location.href = 'https://ia601000.us.archive.org/8/items/whitepaperv-2.0/Whitepaperv2.0.pdf';
  }, []);
  
  return (
    <div className="flex justify-center items-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500 mx-auto mb-4"></div>
        <p className="text-[#f5f5f5]">Redirecting to whitepaper...</p>
      </div>
    </div>
  );
};

// Loading component for suspense fallback
const LoadingComponent = () => (
  <div className="flex justify-center items-center min-h-screen">
    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
  </div>
);

export default function App() {
  return (
    <Router>
      <Suspense fallback={<LoadingComponent />}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/faq" element={<FAQPage />} />
          <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
          <Route path="/terms" element={<TermsPage />} />
          <Route path="/oauth/callback" element={<OAuthRedirect />} />
          {/* Redirect any PDF requests to archive.org */}
          <Route path="/Whitepaperv2.0.pdf" element={<PDFRedirect />} />
        </Routes>
      </Suspense>
    </Router>
  );
}