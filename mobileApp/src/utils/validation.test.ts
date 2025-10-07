import { validateEmail, validatePassword, validateName } from './validation';

console.log('=== Email Validation Tests ===');

const testEmails = [
  { email: 'test@ekehi.network', expected: true },
  { email: 'user@example.com', expected: true },
  { email: 'invalid.email', expected: false },
  { email: '@example.com', expected: false },
  { email: 'user@', expected: false },
  { email: 'user@.com', expected: false },
  { email: '', expected: false },
  { email: 'user@example.', expected: false }
];

testEmails.forEach(({ email, expected }) => {
  const result = validateEmail(email);
  const status = result === expected ? '✅ PASS' : '❌ FAIL';
  console.log(`${status} Email: "${email}" | Expected: ${expected} | Got: ${result}`);
});

console.log('\n=== Password Validation Tests ===');

const testPasswords = [
  { password: '123456', expected: true, message: '' },
  { password: 'short', expected: false, message: 'Password must be at least 6 characters long' },
  { password: 'a'.repeat(129), expected: false, message: 'Password must be less than 128 characters' },
  { password: '', expected: false, message: 'Password must be at least 6 characters long' }
];

testPasswords.forEach(({ password, expected, message }) => {
  const result = validatePassword(password);
  const status = result.isValid === expected ? '✅ PASS' : '❌ FAIL';
  console.log(`${status} Password: "${password}" | Expected: ${expected} | Got: ${result.isValid}`);
  if (message && result.message !== message) {
    console.log(`  ❌ Message mismatch. Expected: "${message}" | Got: "${result.message}"`);
  }
});

console.log('\n=== Name Validation Tests ===');

const testNames = [
  { name: 'John Doe', expected: true, message: '' },
  { name: 'A', expected: false, message: 'Name must be at least 2 characters long' },
  { name: '123', expected: false, message: 'Name can only contain letters and spaces' },
  { name: 'John123', expected: false, message: 'Name can only contain letters and spaces' },
  { name: '', expected: false, message: 'Name must be at least 2 characters long' },
  { name: 'a'.repeat(51), expected: false, message: 'Name must be less than 50 characters' }
];

testNames.forEach(({ name, expected, message }) => {
  const result = validateName(name);
  const status = result.isValid === expected ? '✅ PASS' : '❌ FAIL';
  console.log(`${status} Name: "${name}" | Expected: ${expected} | Got: ${result.isValid}`);
  if (message && result.message !== message) {
    console.log(`  ❌ Message mismatch. Expected: "${message}" | Got: "${result.message}"`);
  }
});

console.log('\n=== Test Complete ===');