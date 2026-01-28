import { Play, Info } from 'lucide-react';
import { Button } from '@/app/components/ui/button';

interface HeroSectionProps {
  title: string;
  description: string;
  imageUrl: string;
}

export function HeroSection({ title, description, imageUrl }: HeroSectionProps) {
  return (
    <div className="relative h-[80vh] w-full">
      {/* Background Image with Gradient Overlay */}
      <div className="absolute inset-0">
        <img
          src={imageUrl}
          alt={title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black via-black/50 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-r from-black/80 via-transparent to-transparent" />
      </div>

      {/* Content */}
      <div className="relative h-full flex items-center px-8 md:px-16 max-w-7xl">
        <div className="max-w-xl space-y-4">
          <h1 className="text-4xl md:text-6xl font-bold text-white">
            {title}
          </h1>
          <p className="text-lg md:text-xl text-gray-200">
            {description}
          </p>
          <div className="flex gap-3 pt-4">
            <Button 
              size="lg" 
              className="bg-white text-black hover:bg-gray-200 gap-2"
            >
              <Play className="w-5 h-5 fill-current" />
              Play
            </Button>
            <Button 
              size="lg" 
              variant="secondary"
              className="bg-gray-500/70 text-white hover:bg-gray-500/50 gap-2"
            >
              <Info className="w-5 h-5" />
              More Info
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
