// Simple email validation test
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

console.log('=== Email Validation Test ===');
console.log('Testing various email formats...\n');

const testEmails = [
  'test@ekehi.network',
  'user@example.com',
  'invalid.email',
  '@example.com',
  'user@',
  'user@.com',
  '',
  'user@example.',
  'valid.email@domain.co.uk',
  'another+tag@example.org'
];

testEmails.forEach(email => {
  const isValid = validateEmail(email);
  const status = isValid ? '✅ VALID' : '❌ INVALID';
  console.log(`${status} ${email || '(empty string)'}`);
});

console.log('\n✅ Email validation implementation complete!');