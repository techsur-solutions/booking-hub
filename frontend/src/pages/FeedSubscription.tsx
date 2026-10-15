import { useState } from 'react';
import { useLocations } from '../hooks/useLocations';
import { feedsApi } from '../api/feeds';

interface FeedSectionProps {
  title: string;
  url: string;
  instructions: string;
  testId: string;
}

function useCopyToClipboard() {
  const [copiedUrl, setCopiedUrl] = useState<string | null>(null);

  async function copyToClipboard(text: string) {
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(text);
      } else {
        // Fallback for older browsers
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }
      setCopiedUrl(text);
      // Reset "Copied!" toast after 2 seconds
      setTimeout(() => setCopiedUrl(null), 2000);
    } catch {
      // Silently fail — clipboard may be denied in certain contexts
    }
  }

  return { copiedUrl, copyToClipboard };
}

function FeedSection({ title, url, instructions, testId }: FeedSectionProps) {
  const { copiedUrl, copyToClipboard } = useCopyToClipboard();
  const isCopied = copiedUrl === url;

  return (
    <section
      data-testid={testId}
      style={{
        background: 'var(--color-bg-alt)',
        border: '1px solid var(--color-border)',
        borderRadius: 'var(--border-radius-md)',
        padding: 'var(--spacing-lg)',
        marginBottom: 'var(--spacing-lg)',
      }}
    >
      <h2
        style={{
          fontSize: 'var(--font-size-xl)',
          fontWeight: 'var(--font-weight-semibold)',
          color: 'var(--color-text)',
          margin: '0 0 var(--spacing-sm) 0',
        }}
      >
        {title}
      </h2>

      {/* URL display + copy/preview actions */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)',
          flexWrap: 'wrap',
          marginBottom: 'var(--spacing-sm)',
        }}
      >
        <code
          data-testid={`${testId}-url`}
          style={{
            flex: 1,
            minWidth: 200,
            padding: 'var(--spacing-xs) var(--spacing-sm)',
            background: 'var(--color-bg)',
            border: '1px solid var(--color-border)',
            borderRadius: 'var(--border-radius-sm)',
            fontSize: 'var(--font-size-sm)',
            fontFamily: 'var(--font-family-mono)',
            color: 'var(--color-text)',
            wordBreak: 'break-all',
          }}
        >
          {url}
        </code>

        {/* Copy button */}
        <button
          type="button"
          data-testid={`${testId}-copy`}
          onClick={() => copyToClipboard(url)}
          aria-label={`Copy ${title} URL to clipboard`}
          style={{
            padding: 'var(--spacing-xs) var(--spacing-md)',
            background: isCopied ? 'var(--color-success)' : 'var(--color-primary)',
            color: '#ffffff',
            border: 'none',
            borderRadius: 'var(--border-radius)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-medium)',
            cursor: 'pointer',
            whiteSpace: 'nowrap',
            transition: 'background var(--transition-base)',
          }}
        >
          {isCopied ? '✓ Copied!' : 'Copy'}
        </button>

        {/* Preview button */}
        <a
          href={url}
          target="_blank"
          rel="noopener noreferrer"
          data-testid={`${testId}-preview`}
          aria-label={`Preview ${title} feed in new tab`}
          style={{
            padding: 'var(--spacing-xs) var(--spacing-md)',
            background: 'transparent',
            color: 'var(--color-primary)',
            border: '1px solid var(--color-primary)',
            borderRadius: 'var(--border-radius)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-medium)',
            textDecoration: 'none',
            whiteSpace: 'nowrap',
          }}
          onClick={(e) => {
            // Trigger window.open for testability (Playwright can intercept)
            e.preventDefault();
            window.open(url, '_blank', 'noopener,noreferrer');
          }}
        >
          Preview ↗
        </a>
      </div>

      {/* Usage instructions */}
      <p
        style={{
          fontSize: 'var(--font-size-sm)',
          color: 'var(--color-text-light)',
          margin: 0,
          lineHeight: 'var(--line-height)',
        }}
      >
        {instructions}
      </p>
    </section>
  );
}

/**
 * FeedSubscription
 *
 * Lists RSS2, iCal, and JSON feed URLs with copy-to-clipboard buttons.
 * Displays a location filter dropdown that parameterises all three URLs
 * with ?location_id=X when a location is selected.
 *
 * No authentication required — all feeds are publicly accessible.
 */
export function FeedSubscription() {
  const [selectedLocationId, setSelectedLocationId] = useState<string>('');
  const { data: locations = [] } = useLocations();

  // Build location filter param (undefined = no filter)
  const locationParam = selectedLocationId
    ? { location_id: selectedLocationId }
    : undefined;

  const rss2Url = feedsApi.getRss2FeedUrl(locationParam);
  const icalUrl = feedsApi.getICalFeedUrl(locationParam);
  const jsonUrl = feedsApi.getJsonFeedUrl(locationParam);

  return (
    <div
      data-testid="feed-subscription"
      style={{
        maxWidth: 780,
        margin: '0 auto',
        padding: 'var(--spacing-xl)',
        fontFamily: 'var(--font-family)',
      }}
    >
      <h1
        style={{
          fontSize: 'var(--font-size-3xl)',
          fontWeight: 'var(--font-weight-bold)',
          color: 'var(--color-text)',
          marginBottom: 'var(--spacing-lg)',
          marginTop: 0,
        }}
      >
        Feed Subscription
      </h1>

      {/* No authentication required banner */}
      <div
        role="note"
        data-testid="no-auth-banner"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)',
          padding: 'var(--spacing-sm) var(--spacing-md)',
          background: 'rgba(16, 185, 129, 0.1)',
          border: '1px solid rgba(16, 185, 129, 0.3)',
          borderRadius: 'var(--border-radius)',
          marginBottom: 'var(--spacing-xl)',
          fontSize: 'var(--font-size-sm)',
          color: 'var(--color-success-dark)',
        }}
      >
        <span aria-hidden="true" style={{ fontSize: 'var(--font-size-lg)' }}>🔓</span>
        <span>
          <strong>No authentication required.</strong>{' '}
          These feeds are publicly accessible. No login required.
        </span>
      </div>

      {/* Location filter */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <label
          htmlFor="location-filter"
          style={{
            display: 'block',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-medium)',
            color: 'var(--color-text)',
            marginBottom: 'var(--spacing-xs)',
          }}
        >
          Filter by Location (optional)
        </label>
        <select
          id="location-filter"
          data-testid="location-filter"
          value={selectedLocationId}
          onChange={(e) => setSelectedLocationId(e.target.value)}
          style={{
            padding: 'var(--spacing-xs) var(--spacing-sm)',
            border: '1px solid var(--color-border)',
            borderRadius: 'var(--border-radius)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--color-text)',
            background: 'var(--color-bg)',
            minWidth: 200,
          }}
        >
          <option value="">All locations</option>
          {locations.map((loc) => (
            <option key={loc.id} value={loc.id}>
              {loc.name}
            </option>
          ))}
        </select>
      </div>

      {/* Feed sections */}

      {/* RSS2 Feed — /feeds/rss2 */}
      <FeedSection
        title="RSS2 Feed"
        url={rss2Url}
        instructions="Subscribe in RSS reader apps like Feedly or Inoreader. Paste this URL into your reader's 'Add feed' dialog."
        testId="rss2-feed"
      />

      {/* iCal Feed — /feeds/ical */}
      <FeedSection
        title="iCal Feed"
        url={icalUrl}
        instructions={
          'Subscribe in Outlook: File → Account Settings → Internet Calendars → New. ' +
          'In Google Calendar: Other Calendars → From URL → paste this URL.'
        }
        testId="ical-feed"
      />

      {/* JSON Feed — /feeds/json */}
      <FeedSection
        title="JSON Feed"
        url={jsonUrl}
        instructions="Use this URL for custom integrations or API consumers. Returns approved bookings as JSON."
        testId="json-feed"
      />
    </div>
  );
}
