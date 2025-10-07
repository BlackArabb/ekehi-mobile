import { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert, Linking, Image } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { ExternalLink, Check, Clock, MessageCircle } from 'lucide-react-native';
import { SocialTask } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'expo-router';
import { databases, appwriteConfig } from '@/config/appwrite';
import { ID, Query } from 'appwrite';
import PulseLoader from '@/components/PulseLoader';
import LoadingDots from '@/components/LoadingDots';

// Function to extract domain from URL
const extractDomain = (url: string): string => {
  try {
    const domain = new URL(url).hostname;
    return domain.startsWith('www.') ? domain.substring(4) : domain;
  } catch (error) {
    return 'unknown';
  }
};

// Function to generate favicon URL
const getFaviconUrl = (url: string): string => {
  try {
    const domain = new URL(url).hostname;
    return `https://www.google.com/s2/favicons?domain=${domain}&sz=64`;
  } catch (error) {
    return 'https://www.google.com/s2/favicons?domain=example.com&sz=64';
  }
};

export default function SocialPage() {
  const router = useRouter();
  const { user } = useAuth();
  const insets = useSafeAreaInsets();
  const [tasks, setTasks] = useState<SocialTask[]>([]);
  const [completingTasks, setCompletingTasks] = useState<Set<string>>(new Set());
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      // Add a small delay to ensure router is ready
      setTimeout(() => {
        router.replace('/');
      }, 100);
      return;
    }
    fetchTasks();
  }, [user]);

  const fetchTasks = async () => {
    try {
      // Fetch social tasks from Appwrite database
      const tasksResponse = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.socialTasks,
        [Query.orderDesc('sortOrder')]
      );

      // Fetch user's completed tasks
      if (user) {
        const userTasksResponse = await databases.listDocuments(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userSocialTasks,
          [Query.equal('userId', [user.id])]
        );

        // Create a set of completed task IDs
        const completedTaskIds = new Set(
          userTasksResponse.documents.map(doc => doc.taskId)
        );

        // Map tasks with completion status
        const tasksWithStatus = tasksResponse.documents.map(task => ({
          id: task.$id,
          title: task.title,
          description: task.description,
          platform: task.platform,
          taskType: task.taskType,
          rewardCoins: task.rewardCoins,
          actionUrl: task.actionUrl,
          verificationMethod: task.verificationMethod,
          isActive: task.isActive,
          sortOrder: task.sortOrder,
          isCompleted: completedTaskIds.has(task.$id)
        } as SocialTask));

        setTasks(tasksWithStatus);
      }
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTaskAction = async (task: SocialTask) => {
    if (task.isCompleted) return;

    setCompletingTasks(prev => new Set(prev).add(task.id));

    try {
      // Open the task URL
      if (task.actionUrl) {
        await Linking.openURL(task.actionUrl);
      }

      // Handle different verification methods
      switch (task.verificationMethod) {
        case 'manual':
          // For manual verification, prompt the user to confirm completion
          Alert.alert(
            'Task Verification',
            `Please complete the task "${task.title}" and then confirm completion below.`,
            [
              {
                text: 'Cancel',
                style: 'cancel',
                onPress: () => {
                  setCompletingTasks(prev => {
                    const newSet = new Set(prev);
                    newSet.delete(task.id);
                    return newSet;
                  });
                }
              },
              {
                text: 'I have completed the task',
                style: 'default',
                onPress: async () => {
                  await completeTask(task);
                }
              }
            ]
          );
          break;
          
        case 'auto':
          // For auto verification, we might check an API or use some other method
          // For now, we'll just wait a bit and then mark as completed
          Alert.alert(
            'Task Started',
            `Please complete the task "${task.title}". We'll check for completion shortly.`,
            [
              {
                text: 'OK',
                onPress: async () => {
                  // Simulate some processing time
                  await new Promise(resolve => setTimeout(() => resolve(undefined), 3000));
                  await completeTask(task);
                }
              }
            ]
          );
          break;
          
        default:
          // Default to manual verification for unknown methods
          Alert.alert(
            'Task Verification',
            `Please complete the task "${task.title}" and then confirm completion below.`,
            [
              {
                text: 'Cancel',
                style: 'cancel',
                onPress: () => {
                  setCompletingTasks(prev => {
                    const newSet = new Set(prev);
                    newSet.delete(task.id);
                    return newSet;
                  });
                }
              },
              {
                text: 'I have completed the task',
                style: 'default',
                onPress: async () => {
                  await completeTask(task);
                }
              }
            ]
          );
      }
    } catch (error) {
      console.error('Failed to open URL:', error);
      setCompletingTasks(prev => {
        const newSet = new Set(prev);
        newSet.delete(task.id);
        return newSet;
      });
      Alert.alert('Error', 'Failed to open task URL. Please try again.');
    }
  };

  const completeTask = async (task: SocialTask) => {
    try {
      if (!user) return;
      
      // Create user task completion record
      await databases.createDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userSocialTasks,
        ID.unique(),
        {
          userId: user.id,
          taskId: task.id,
          completedAt: new Date().toISOString()
        }
      );

      // Update user profile with reward
      // First, we need to get the current user profile
      const profileResponse = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [user.id])]
      );

      if (profileResponse.documents.length > 0) {
        const profileDoc = profileResponse.documents[0];
        
        // Update the profile with the new coin balance
        await databases.updateDocument(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          profileDoc.$id,
          {
            totalCoins: profileDoc.totalCoins + task.rewardCoins,
            lifetimeEarnings: profileDoc.lifetimeEarnings + task.rewardCoins,
            todayEarnings: profileDoc.todayEarnings + task.rewardCoins,
            updatedAt: new Date().toISOString()
          }
        );
      }

      // Update the UI to show the task as completed
      setTasks(prev => 
        prev.map(t => 
          t.id === task.id ? { ...t, isCompleted: true } : t
        )
      );
      
      Alert.alert('Success!', `You earned ${task.rewardCoins} EKH tokens!`);
    } catch (error) {
      console.error('Failed to complete task:', error);
      Alert.alert('Error', 'Failed to complete task. Please try again.');
    } finally {
      setCompletingTasks(prev => {
        const newSet = new Set(prev);
        newSet.delete(task.id);
        return newSet;
      });
    }
  };

  const getPlatformColors = (platform: string): [string, string] => {
    switch (platform.toLowerCase()) {
      case 'twitter':
        return ['#1da1f2', '#0d8bd9'];
      case 'youtube':
        return ['#ff0000', '#cc0000'];
      case 'telegram':
        return ['#0088cc', '#006699'];
      case 'discord':
        return ['#5865f2', '#4752c4'];
      case 'facebook':
        return ['#1877f2', '#0c5fc9'];
      case 'instagram':
        return ['#e1306c', '#c13584'];
      case 'tiktok':
        return ['#000000', '#333333'];
      case 'reddit':
        return ['#ff4500', '#cc3700'];
      case 'linkedin':
        return ['#0077b5', '#005885'];
      default:
        return ['#6b7280', '#4b5563'];
    }
  };

  if (isLoading) {
    return (
      <LinearGradient colors={['#0F172A', '#1E293B', '#334155']} style={styles.container}>
        <View style={styles.loadingContainer}>
          <PulseLoader />
        </View>
      </LinearGradient>
    );
  }

  const completedTasks = tasks.filter(task => task.isCompleted).length;
  const totalRewards = tasks.filter(task => task.isCompleted).reduce((sum, task) => sum + task.rewardCoins, 0);

  return (
    <LinearGradient colors={['#0F172A', '#1E293B']} style={styles.container}>
      <ScrollView 
        style={[styles.scrollView, { paddingTop: insets.top }]}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>Social Tasks</Text>
          <Text style={styles.subtitle}>Complete tasks to earn extra EKH tokens</Text>
        </View>

        {/* Stats */}
        <View style={styles.statsContainer}>
          <View style={styles.statCard}>
            <Text style={styles.statValue}>{completedTasks}/{tasks.length}</Text>
            <Text style={styles.statLabel}>Completed</Text>
          </View>
          <View style={styles.statCard}>
            <Text style={styles.statValue}>{totalRewards}</Text>
            <Text style={styles.statLabel}>EKH Earned</Text>
          </View>
        </View>

        {/* Tasks List */}
        <View style={styles.tasksContainer}>
          {tasks.length === 0 ? (
            <View style={styles.emptyContainer}>
              <MessageCircle size={64} color="rgba(255, 255, 255, 0.3)" />
              <Text style={styles.emptyTitle}>No Tasks Available</Text>
              <Text style={styles.emptyText}>Check back later for new social tasks</Text>
            </View>
          ) : (
            tasks.map((task) => {
              const [primaryColor, secondaryColor] = getPlatformColors(task.platform);
              const isCompleting = completingTasks.has(task.id);
              
              return (
                <View key={task.id} style={styles.taskCard}>
                  <View style={styles.taskHeader}>
                    <View style={[styles.platformIcon, { backgroundColor: primaryColor }]}>
                      {task.actionUrl ? (
                        <Image 
                          source={{ uri: getFaviconUrl(task.actionUrl) }} 
                          style={styles.favicon}
                          onError={(error) => console.log('Favicon load error:', error)}
                        />
                      ) : (
                        <ExternalLink size={24} color="#ffffff" />
                      )}
                    </View>
                    <View style={styles.taskInfo}>
                      <Text style={styles.taskTitle}>{task.title}</Text>
                      <Text style={styles.taskDescription}>{task.description}</Text>
                      {task.actionUrl && (
                        <Text style={styles.taskDomain}>{extractDomain(task.actionUrl)}</Text>
                      )}
                    </View>
                  </View>
                  
                  <View style={styles.taskFooter}>
                    <View style={styles.rewardContainer}>
                      <Text style={styles.rewardValue}>+{task.rewardCoins}</Text>
                      <Text style={styles.rewardLabel}>EKH</Text>
                    </View>
                    
                    <TouchableOpacity
                      onPress={() => handleTaskAction(task)}
                      disabled={task.isCompleted || isCompleting}
                      style={styles.actionButton}
                    >
                      <LinearGradient
                        colors={
                          task.isCompleted
                            ? ['#10b981', '#059669']
                            : isCompleting
                            ? ['#f59e0b', '#d97706']
                            : [primaryColor, secondaryColor]
                        }
                        style={styles.actionButtonGradient}
                      >
                        {task.isCompleted ? (
                          <>
                            <Check size={16} color="#ffffff" />
                            <Text style={styles.actionButtonText}>Completed</Text>
                          </>
                        ) : isCompleting ? (
                          <>
                            <Clock size={16} color="#ffffff" />
                            <LoadingDots color="#ffffff" size={8} />
                          </>
                        ) : (
                          <>
                            <ExternalLink size={16} color="#ffffff" />
                            <Text style={styles.actionButtonText}>Complete</Text>
                          </>
                        )}
                      </LinearGradient>
                    </TouchableOpacity>
                  </View>
                </View>
              );
            })
          )}
        </View>
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    paddingHorizontal: 20,
    paddingBottom: 100,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: 20,
  },

  header: {
    alignItems: 'center',
    marginBottom: 24,
    marginTop: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 24,
    gap: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  statCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    alignItems: 'center',
    flex: 1,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  statValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  statLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 4,
  },
  tasksContainer: {
    gap: 16,
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: 48,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 16,
    marginBottom: 8,
  },
  emptyText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.4)',
    textAlign: 'center',
  },
  taskCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
    marginBottom: 12,
  },
  taskHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  platformIcon: {
    width: 52,
    height: 52,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
    overflow: 'hidden',
  },
  favicon: {
    width: 32,
    height: 32,
    borderRadius: 8,
  },
  taskInfo: {
    flex: 1,
  },
  taskTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#ffffff',
    marginBottom: 4,
  },
  taskDescription: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    lineHeight: 20,
    marginBottom: 4,
  },
  taskDomain: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.5)',
    fontStyle: 'italic',
  },
  taskFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  rewardContainer: {
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: 4,
  },
  rewardValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffa000',
  },
  rewardLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  actionButton: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  actionButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    gap: 6,
  },
  actionButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffffff',
  },
});
