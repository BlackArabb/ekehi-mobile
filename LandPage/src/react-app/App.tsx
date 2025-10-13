import { BrowserRouter as Router, Routes, Route } from "react-router";
import { lazy, Suspense } from "react";

// Lazy load route components
const HomePage = lazy(() => import("@/react-app/pages/Home"));
const FAQPage = lazy(() => import("@/react-app/pages/FAQ"));
const PrivacyPolicyPage = lazy(() => import("@/react-app/pages/PrivacyPolicy"));
const TermsPage = lazy(() => import("@/react-app/pages/Terms"));
const OAuthRedirect = lazy(() => import("@/react-app/pages/OAuthRedirect"));

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
        </Routes>
      </Suspense>
    </Router>
  );
}