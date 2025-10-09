import { BrowserRouter as Router, Routes, Route } from "react-router";
import HomePage from "@/react-app/pages/Home";
import FAQPage from "@/react-app/pages/FAQ";
import PrivacyPolicyPage from "@/react-app/pages/PrivacyPolicy";
import TermsPage from "@/react-app/pages/Terms";

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/faq" element={<FAQPage />} />
        <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
        <Route path="/terms" element={<TermsPage />} />
      </Routes>
    </Router>
  );
}