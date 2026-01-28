import { useRef } from 'react';
import { ChevronLeft, ChevronRight, Sparkles } from 'lucide-react';

interface MediaItem {
  id: number;
  title: string;
  imageUrl: string;
}

interface SpecialSectionProps {
  title: string;
  items: MediaItem[];
  accentColor?: string;
}

export function SpecialSection({ title, items, accentColor = '#e50914' }: SpecialSectionProps) {
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
    <div className="px-8 md:px-16 py-8 relative group">
      {/* Special Header with Icon */}
      <div className="flex items-center gap-3 mb-4">
        <Sparkles className="w-6 h-6" style={{ color: accentColor }} />
        <h2 
          className="text-2xl md:text-3xl font-semibold"
          style={{ color: accentColor }}
        >
          {title}
        </h2>
      </div>

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
        className="flex gap-3 overflow-x-auto scrollbar-hide scroll-smooth"
      >
        {items.map((item) => (
          <div
            key={item.id}
            className="relative flex-shrink-0 w-64 md:w-80 cursor-pointer group/item"
          >
            {/* Special Border Effect */}
            <div 
              className="absolute inset-0 rounded-lg opacity-0 group-hover/item:opacity-100 transition-opacity"
              style={{
                padding: '2px',
                background: `linear-gradient(135deg, ${accentColor}, transparent)`,
              }}
            >
              <div className="w-full h-full bg-black rounded-lg" />
            </div>

            {/* Content Card */}
            <div className="relative aspect-video rounded-lg overflow-hidden transition-transform group-hover/item:scale-105">
              <img
                src={item.imageUrl}
                alt={item.title}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black via-black/20 to-transparent" />
              
              {/* Title Overlay */}
              <div className="absolute bottom-0 left-0 right-0 p-4">
                <h3 className="text-white font-semibold text-lg line-clamp-2">
                  {item.title}
                </h3>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
