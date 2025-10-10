import { useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Animated, Dimensions } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { CheckCircle, XCircle, AlertTriangle, Info, X, RefreshCw } from 'lucide-react-native';
import { useNotifications } from '@/contexts/NotificationContext';

const { width } = Dimensions.get('window');

const iconMap = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
};

const colorMap = {
  success: {
    colors: ['rgba(16, 185, 129, 0.2)', 'rgba(5, 150, 105, 0.2)'],
    borderColor: 'rgba(16, 185, 129, 0.4)',
    textColor: '#10b981',
    iconColor: '#10b981'
  },
  error: {
    colors: ['rgba(239, 68, 68, 0.2)', 'rgba(220, 38, 38, 0.2)'],
    borderColor: 'rgba(239, 68, 68, 0.4)',
    textColor: '#ef4444',
    iconColor: '#ef4444'
  },
  warning: {
    colors: ['rgba(245, 158, 11, 0.2)', 'rgba(217, 119, 6, 0.2)'],
    borderColor: 'rgba(245, 158, 11, 0.4)',
    textColor: '#f59e0b',
    iconColor: '#f59e0b'
  },
  info: {
    colors: ['rgba(59, 130, 246, 0.2)', 'rgba(37, 99, 235, 0.2)'],
    borderColor: 'rgba(59, 130, 246, 0.4)',
    textColor: '#3b82f6',
    iconColor: '#3b82f6'
  },
};

export default function NotificationSystem() {
  const { notifications, removeNotification } = useNotifications();

  if (notifications.length === 0) return null;

  return (
    <View style={styles.container}>
      {notifications.map((notification) => {
        const Icon = iconMap[notification.type];
        const colors = colorMap[notification.type];

        return (
          <NotificationItem
            key={notification.id}
            notification={notification}
            Icon={Icon}
            colors={colors}
            onDismiss={() => removeNotification(notification.id)}
          />
        );
      })}
    </View>
  );
}

interface NotificationItemProps {
  notification: any;
  Icon: any;
  colors: any;
  onDismiss: () => void;
}

function NotificationItem({ notification, Icon, colors, onDismiss }: NotificationItemProps) {
  const slideAnim = new Animated.Value(width);
  const opacityAnim = new Animated.Value(0);

  useEffect(() => {
    // Slide in animation
    Animated.parallel([
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }),
      Animated.timing(opacityAnim, {
        toValue: 1,
        duration: 300,
        useNativeDriver: true,
      }),
    ]).start();

    // Auto dismiss after duration
    let timer: ReturnType<typeof setTimeout> | null = null;
    if (notification.duration) {
      timer = setTimeout(() => {
        handleDismiss();
      }, notification.duration);
    }

    return () => {
      if (timer) clearTimeout(timer);
    };
  }, []);

  const handleDismiss = () => {
    Animated.parallel([
      Animated.timing(slideAnim, {
        toValue: width,
        duration: 250,
        useNativeDriver: true,
      }),
      Animated.timing(opacityAnim, {
        toValue: 0,
        duration: 250,
        useNativeDriver: true,
      }),
    ]).start(() => {
      onDismiss();
    });
  };

  const handleRetry = () => {
    if (notification.onRetry) {
      notification.onRetry();
    }
    handleDismiss();
  };

  return (
    <Animated.View
      style={[
        styles.notificationContainer,
        {
          transform: [{ translateX: slideAnim }],
          opacity: opacityAnim,
        },
      ]}
    >
      <LinearGradient
        colors={colors.colors}
        style={[styles.notification, { borderColor: colors.borderColor }]}
      >
        <View style={styles.content}>
          <View style={styles.iconContainer}>
            <Icon size={24} color={colors.iconColor} />
          </View>
          
          <View style={styles.textContainer}>
            <Text style={[styles.title, { color: colors.textColor }]}>
              {notification.title}
            </Text>
            {notification.message && (
              <Text style={styles.message}>
                {notification.message}
              </Text>
            )}
          </View>

          <View style={styles.actionsContainer}>
            {notification.onRetry && (
              <TouchableOpacity style={styles.retryButton} onPress={handleRetry}>
                <RefreshCw size={20} color="rgba(255, 255, 255, 0.6)" />
              </TouchableOpacity>
            )}
            <TouchableOpacity style={styles.closeButton} onPress={handleDismiss}>
              <X size={20} color="rgba(255, 255, 255, 0.6)" />
            </TouchableOpacity>
          </View>
        </View>

        {/* Progress bar for auto-hide */}
        {notification.duration && (
          <View style={styles.progressContainer}>
            <Animated.View
              style={[
                styles.progressBar,
                { backgroundColor: colors.iconColor },
              ]}
            />
          </View>
        )}
      </LinearGradient>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 60, // Account for status bar
    left: 16,
    right: 16,
    zIndex: 1000,
    gap: 8,
  },
  notificationContainer: {
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  notification: {
    borderRadius: 16,
    borderWidth: 1,
    overflow: 'hidden',
  },
  content: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    padding: 16,
    gap: 12,
  },
  iconContainer: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  textContainer: {
    flex: 1,
    minWidth: 0,
  },
  title: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 2,
  },
  message: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.8)',
    lineHeight: 18,
  },
  actionsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  retryButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  closeButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  progressContainer: {
    height: 3,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
  },
  progressBar: {
    height: '100%',
    width: '100%',
  },
});