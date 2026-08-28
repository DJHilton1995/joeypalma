export default function CyberButton({ children, color = "red", onClick }) {
  const cls =
    color === "blue"
      ? "cyber-btn-blue"
      : "cyber-btn";

  return (
    <button onClick={onClick} className={cls}>
      {children}
    </button>
  );
}
