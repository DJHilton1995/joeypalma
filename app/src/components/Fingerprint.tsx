export default function Fingerprint({ className = "" }: { className?: string }) {
  return (
    <svg className={`fingerprint ${className}`} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
      <path d="M12 2v4" stroke="#9be7ff" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" opacity="0.9"/>
      <path d="M7 6c1-1 3-2 5-2s4 1 5 2" stroke="#00eaff" strokeWidth="0.9" strokeLinecap="round" strokeLinejoin="round" opacity="0.85"/>
      <path d="M4 12c0-4 4-8 8-8s8 4 8 8c0 4-4 8-8 8s-8-4-8-8z" stroke="#00eaff" strokeWidth="0.8" strokeLinecap="round" strokeLinejoin="round" opacity="0.06"/>
      <path d="M12 8v8" stroke="#9be7ff" strokeWidth="1.1" strokeLinecap="round" strokeLinejoin="round" opacity="0.9"/>
    </svg>
  );
}
