import React, { useEffect, useRef, useState } from 'react';
import { StyleSheet, View, Text, Button, SafeAreaView, ScrollView, TouchableOpacity } from 'react-native';
import * as SQLite from 'expo-sqlite';
import * as Notifications from 'expo-notifications';
import * as TaskManager from 'expo-task-manager';
import * as BackgroundFetch from 'expo-background-fetch';
import { ProgressBar } from 'react-native-paper';
import { StatusBar } from 'expo-status-bar';

const DB = SQLite.openDatabase('fitness.db');
const BACKGROUND_FETCH_TASK = 'fitness-background-fetch';
const MILESTONES = [1000, 5000, 10000, 20000];

function initDb() {
  DB.transaction(tx => {
    tx.executeSql(
      'CREATE TABLE IF NOT EXISTS steps (id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp INTEGER, steps INTEGER);'
    );
  });
}

function insertSteps(steps) {
  const ts = Date.now();
  DB.transaction(tx => {
    tx.executeSql('INSERT INTO steps (timestamp, steps) values (?, ?)', [ts, steps]);
  });
}

function getTotalStepsAsync() {
  return new Promise(resolve => {
    DB.transaction(tx => {
      tx.executeSql('SELECT SUM(steps) as total FROM steps', [], (_, { rows }) => {
        const val = rows._array[0]?.total ?? 0;
        resolve(val);
      });
    });
  });
}

async function registerBackgroundFetchAsync() {
  return BackgroundFetch.registerTaskAsync(BACKGROUND_FETCH_TASK, {
    minimumInterval: 60 * 15, // 15 minutes
    stopOnTerminate: false,
    startOnBoot: true,
  });
}

TaskManager.defineTask(BACKGROUND_FETCH_TASK, async () => {
  try {
    // Simulate background step addition
    const simulated = Math.floor(Math.random() * 40) + 10;
    insertSteps(simulated);
    console.log('[BackgroundFetch] added steps', simulated);
    return BackgroundFetch.Result.NewData;
  } catch (err) {
    return BackgroundFetch.Result.Failed;
  }
});

export default function App() {
  const [total, setTotal] = useState(0);
  const [running, setRunning] = useState(true);
  const intervalRef = useRef(null);

  useEffect(() => {
    initDb();
    loadTotal();

    // Register notification handler
    Notifications.setNotificationHandler({
      handleNotification: async () => ({ shouldShowAlert: true, shouldPlaySound: false, shouldSetBadge: false }),
    });

    // Try to register background fetch (may not run on Expo Go)
    registerBackgroundFetchAsync().catch(() => {});

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, []);

  useEffect(() => {
    if (running) {
      intervalRef.current = setInterval(async () => {
        const add = Math.floor(Math.random() * 10) + 5;
        insertSteps(add);
        const t = await getTotalStepsAsync();
        setTotal(t);
        maybeNotifyMilestone(t);
      }, 2000);
    } else {
      if (intervalRef.current) clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [running]);

  async function loadTotal() {
    const t = await getTotalStepsAsync();
    setTotal(t);
  }

  async function maybeNotifyMilestone(t) {
    for (let m of MILESTONES) {
      if (t >= m && t - Math.random() * 30 < m) {
        await Notifications.scheduleNotificationAsync({
          content: { title: 'Milestone!', body: `You reached ${m} steps!` },
          trigger: null,
        });
      }
    }
  }

  function reset() {
    DB.transaction(tx => tx.executeSql('DELETE FROM steps'));
    setTotal(0);
  }

  const progress = Math.min(total / 10000, 1);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="dark" />
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Fitness Tracker</Text>
        <View style={styles.card}>
          <Text style={styles.steps}>{total} steps</Text>
          <ProgressBar progress={progress} color="#05b6c8" style={styles.progress} />
          <View style={styles.row}>
            <Button title={running ? 'Pause' : 'Resume'} onPress={() => setRunning(!running)} />
            <Button title="Reset" onPress={reset} />
            <Button title="Sync WearOS" onPress={() => alert('Sync simulated (mock)')} />
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.subtitle}>Badges</Text>
          <View style={styles.badges}>
            {MILESTONES.map(m => (
              <View key={m} style={styles.badge}>
                <Text style={{ fontWeight: '700' }}>{m}</Text>
                <Text>{total >= m ? 'Unlocked' : 'Locked'}</Text>
              </View>
            ))}
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.subtitle}>Bound Service (Mock)</Text>
          <Text>Tap to fetch live stats from the background service (simulated):</Text>
          <TouchableOpacity style={styles.mockButton} onPress={loadTotal}>
            <Text style={{ color: '#fff' }}>Get Steps Now</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.card}>
          <Text style={styles.subtitle}>Advanced (Gemini)</Text>
          <Text>Send steps to Gemini API (mock):</Text>
          <TouchableOpacity style={styles.mockButton} onPress={() => alert("Gemini suggests: 'Try a 20-min jog'") }>
            <Text style={{ color: '#fff' }}>Send Data</Text>
          </TouchableOpacity>
        </View>

      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f7f7f7' },
  content: { padding: 16 },
  title: { fontSize: 22, fontWeight: '700', marginBottom: 12 },
  card: { backgroundColor: '#fff', borderRadius: 10, padding: 12, marginBottom: 12, elevation: 2 },
  steps: { fontSize: 28, fontWeight: '700', marginBottom: 8 },
  progress: { height: 10, borderRadius: 8, marginBottom: 8 },
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  subtitle: { fontSize: 16, fontWeight: '600', marginBottom: 8 },
  badges: { flexDirection: 'row', justifyContent: 'space-between' },
  badge: { alignItems: 'center', padding: 8, width: '23%', backgroundColor: '#fafafa', borderRadius: 8 },
  mockButton: { marginTop: 8, backgroundColor: '#05b6c8', padding: 10, borderRadius: 8, alignItems: 'center' },
});
