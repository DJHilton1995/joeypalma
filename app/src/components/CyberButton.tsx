import React from "react";

type Props = {
  children: React.ReactNode;
  color?: "pink" | "cyan";
  onClick?: () => void;
};

export default function CyberButton({ children, color = "pink", onClick }: Props) {
  const base = "cyber-btn";
  const variant = color === "cyan" ? "cyber-btn-cyan" : "";
  return (
    <button onClick={onClick} className={`${base} ${variant}`}>
      {children}
    </button>
  );
}
