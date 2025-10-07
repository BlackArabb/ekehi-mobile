# Authentication Button Loading States Fix

## Issue
The loading animation and disabled state were being shared between the "Login" button and the "Continue with Google" button. When one button was pressed, both buttons would show the loading animation and become disabled.

## Root Cause
Both buttons were using the same `isLoading` state variable to control their disabled state and loading indicators. This meant that when either button was pressed and set `isLoading` to true, both buttons would be affected.

## Solution
Created separate loading states for each authentication method:

1. **Added separate loading state variables**:
   ```typescript
   const [isEmailAuthLoading, setIsEmailAuthLoading] = useState(false); // For email/password auth
   const [isGoogleAuthLoading, setIsGoogleAuthLoading] = useState(false); // For Google OAuth
   ```

2. **Updated the auto-login useEffect hook** to check both loading states:
   ```typescript
   useEffect(() => {
     // Check if both email and password are filled (likely from auto-fill)
     // Only attempt auto-login if we haven't had a signup error
     if (email && password && !autoFilled && !autoLoginAttempted && !isEmailAuthLoading && !isGoogleAuthLoading && !signupErrorOccurred) {
       // Only trigger auto-login if we have valid credentials
       if (validateEmail(email) && password.length >= 6) {
         setAutoFilled(true);
         setAutoLoginAttempted(true);
         // Small delay to ensure fields are properly populated
         setTimeout(() => {
           handleEmailAuth();
         }, 300);
       }
     }
   }, [email, password, autoFilled, autoLoginAttempted, isEmailAuthLoading, isGoogleAuthLoading, signupErrorOccurred]);
   ```

3. **Updated the Email Authentication button** to use its own loading state:
   ```jsx
   <TouchableOpacity
     style={[styles.button, isEmailAuthLoading && styles.buttonDisabled]}
     onPress={handleEmailAuth}
     disabled={isEmailAuthLoading}
   >
     {isEmailAuthLoading ? (
       <LoadingDots color="#000000" size={8} />
     ) : (
       <Text style={styles.buttonText}>
         {isSignUp ? 'Sign Up' : 'Sign In'}
       </Text>
     )}
   </TouchableOpacity>
   ```

4. **Updated the Google Authentication button** to use its own loading state:
   ```jsx
   <TouchableOpacity
     style={[styles.googleButton, isGoogleAuthLoading && styles.buttonDisabled]}
     onPress={handleGoogleSignIn}
     disabled={isGoogleAuthLoading}
   >
     {isGoogleAuthLoading ? (
       <LoadingDots color="#000000" size={8} />
     ) : (
       <Text style={styles.googleButtonText}>
         Continue with Google
       </Text>
     )}
   </TouchableOpacity>
   ```

5. **Updated the authentication functions** to use their respective loading states:
   ```typescript
   const handleEmailAuth = async () => {
     // ... validation code ...
     
     setIsEmailAuthLoading(true);
     try {
       // ... authentication logic ...
     } catch (error) {
       // ... error handling ...
     } finally {
       setIsEmailAuthLoading(false);
     }
   };

   const handleGoogleSignIn = async () => {
     setIsGoogleAuthLoading(true);
     try {
       // ... authentication logic ...
     } catch (error) {
       // ... error handling ...
     } finally {
       setIsGoogleAuthLoading(false);
     }
   };
   ```

## Benefits
- Each button now has independent loading state control
- Users can see exactly which action is in progress
- Improved user experience with clearer feedback
- No interference between different authentication methods

## Files Modified
- [app/auth.tsx](file:///c:/ekehi-mobile/app/auth.tsx) - Main authentication screen with separate loading states

## Testing
The fix has been verified to ensure:
1. Email authentication button shows loading state independently
2. Google authentication button shows loading state independently
3. Both buttons can be used sequentially without interference
4. Auto-login functionality still works correctly with the new loading states