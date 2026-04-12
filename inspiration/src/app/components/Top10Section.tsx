import { useRef } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface MediaItem {
  id: number;
  title: string;
  imageUrl: string;
  rank: number;
}

interface Top10SectionProps {
  items: MediaItem[];
}

export function Top10Section({ items }: Top10SectionProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scroll = (direction: 'left' | 'right') => {
    if (scrollRef.current) {
      const scrollAmount = 400;
      scrollRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth',
      });
    }
  };

  return (
    <div className="px-8 md:px-16 py-8 relative group">
      <h2 className="text-2xl md:text-3xl font-semibold text-white mb-4">
        Top 10 Trending
      </h2>

      {/* Navigation Buttons */}
      <button
        onClick={() => scroll('left')}
        className="absolute left-2 top-1/2 -translate-y-1/2 z-10 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
        aria-label="Scroll left"
      >
        <ChevronLeft className="w-6 h-6" />
      </button>
      <button
        onClick={() => scroll('right')}
        className="absolute right-2 top-1/2 -translate-y-1/2 z-10 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
        aria-label="Scroll right"
      >
        <ChevronRight className="w-6 h-6" />
      </button>

      {/* Scrollable Content */}
      <div
        ref={scrollRef}
        className="flex gap-2 overflow-x-auto scrollbar-hide scroll-smooth"
      >
        {items.map((item) => (
          <div
            key={item.id}
            className="relative flex-shrink-0 w-48 md:w-56 cursor-pointer group/item transition-transform hover:scale-105"
          >
            {/* Ranking Number */}
            <div className="absolute -left-4 bottom-0 z-20">
              <div className="relative">
                <svg
                  width="100"
                  height="140"
                  viewBox="0 0 100 140"
                  className="drop-shadow-lg"
                >
                  <text
                    x="50"
                    y="110"
                    fontSize="120"
                    fontWeight="900"
                    fill="#1a1a1a"
                    stroke="#ffffff"
                    strokeWidth="3"
                    textAnchor="middle"
                    className="select-none"
                  >
                    {item.rank}
                  </text>
                </svg>
              </div>
            </div>

            {/* Thumbnail */}
            <div className="relative aspect-[2/3] rounded overflow-hidden">
              <img
                src={item.imageUrl}
                alt={item.title}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover/item:opacity-100 transition-opacity" />
            </div>

            {/* Title */}
            <p className="text-white text-sm mt-2 line-clamp-1">{item.title}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
