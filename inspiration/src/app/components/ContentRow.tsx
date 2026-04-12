import { useRef } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface MediaItem {
  id: number;
  title: string;
  imageUrl: string;
}

interface ContentRowProps {
  title: string;
  items: MediaItem[];
}

export function ContentRow({ title, items }: ContentRowProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scroll = (direction: 'left' | 'right') => {
    if (scrollRef.current) {
      const scrollAmount = 600;
      scrollRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth',
      });
    }
  };

  return (
    <div className="px-8 md:px-16 py-4 relative group">
      <h2 className="text-xl md:text-2xl font-semibold text-white mb-4">
        {title}
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
            className="relative flex-shrink-0 w-48 md:w-60 cursor-pointer group/item transition-transform hover:scale-105"
          >
            <div className="relative aspect-video rounded overflow-hidden">
              <img
                src={item.imageUrl}
                alt={item.title}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover/item:opacity-100 transition-opacity" />
              
              {/* Title on Hover */}
              <div className="absolute bottom-0 left-0 right-0 p-3 opacity-0 group-hover/item:opacity-100 transition-opacity">
                <p className="text-white text-sm font-medium line-clamp-2">
                  {item.title}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
