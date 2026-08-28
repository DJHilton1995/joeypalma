import React from "react";

export default function NeonPanel({ title, children }: { title?: string; children: React.ReactNode }) {
  return (
    <div className="neon-panel rounded-xl-2">
      {title && <h3 className="text-sm font-semibold accent-cyan mb-3">{title}</h3>}
      {children}
    </div>
  );
}
