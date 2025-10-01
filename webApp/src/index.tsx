import React from 'react';
import ReactDOM from 'react-dom/client';
import { MainScreen } from './components/MainScreen/MainScreen.tsx';

const rootElement = document.getElementById('root');
if (!rootElement) throw new Error('Failed to find the root element');

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <MainScreen 
      onProfileClick={() => console.log('Profile clicked')}
      onSearchClick={() => console.log('Search clicked')}
      onContentClick={(content) => console.log('Content clicked:', content.title)}
      onPlayContent={(content) => console.log('Play content:', content.title)}
      onToggleLike={(content) => console.log('Toggle like:', content.title)}
      onToggleWatchlist={(content) => console.log('Toggle watchlist:', content.title)}
    />
  </React.StrictMode>
);