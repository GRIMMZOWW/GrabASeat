export function parseLocalDateTime(isoString) {
  if (!isoString) return null;
  const [datePart, timePart = '00:00:00'] = isoString.split('T');
  const [year, month, day] = datePart.split('-').map(Number);
  const [hour = 0, minute = 0] = timePart.split(':').map(Number);
  return new Date(year, month - 1, day, hour, minute);
}

export function formatDateHeading(dateKey) {
  if (!dateKey) return '';
  const [year, month, day] = dateKey.split('-').map(Number);
  return new Intl.DateTimeFormat('en-IN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(new Date(year, month - 1, day));
}

export function formatShowTime(isoString) {
  const date = parseLocalDateTime(isoString);
  if (!date) return '';
  return new Intl.DateTimeFormat('en-IN', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  }).format(date);
}

export function formatShowDateTime(isoString) {
  const date = parseLocalDateTime(isoString);
  if (!date) return '';
  return new Intl.DateTimeFormat('en-IN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  }).format(date);
}

export function formatCurrency(amount) {
  const value = Number(amount || 0);
  const hasDecimals = value % 1 !== 0;
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: hasDecimals ? 2 : 0,
    maximumFractionDigits: 2
  }).format(value);
}
