export default function NeonPanel({ title, children }) {
  return (
    <div className="neon-panel">
      {title && (
        <h2 className="text-lg font-semibold mb-3 text-[#9bdcff]">
          {title}
        </h2>
      )}
      {children}
    </div>
  );
}
