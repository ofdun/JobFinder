interface QueryStateBlockProps {
  variant: 'loading' | 'error' | 'empty';
  message: string;
  onRetry?: () => void;
}

export function QueryStateBlock({ variant, message, onRetry }: QueryStateBlockProps) {
  const className = `query-state query-state-${variant}`;

  if (variant === 'loading') {
    return (
      <p className={className} aria-live="polite">
        {message}
      </p>
    );
  }

  return (
    <div className={className} role={variant === 'error' ? 'alert' : undefined}>
      <p>{message}</p>
      {variant === 'error' && onRetry ? (
        <button type="button" onClick={onRetry}>
          Повторить
        </button>
      ) : null}
    </div>
  );
}

