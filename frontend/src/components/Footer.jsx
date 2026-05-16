export default function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-2 px-4 py-6 text-sm text-slate-500 sm:flex-row">
        <p>(c) {new Date().getFullYear()} Hotel Reserve. All rights reserved.</p>
        <p>Built with Spring Boot, Kafka, React & Tailwind.</p>
      </div>
    </footer>
  );
}
