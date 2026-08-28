const memoryStorage = new Map();

const isStorageAvailable = () => {
  try {
    const testKey = '__test__';
    window.localStorage.setItem(testKey, testKey);
    window.localStorage.removeItem(testKey);
    return true;
  } catch (e) {
    return false;
  }
};

const fallbackKeys = new Set();
const hasGlobalStorage = isStorageAvailable();

export const safeStorage = {
  getItem: (key) => {
    if (!hasGlobalStorage || fallbackKeys.has(key)) {
      return memoryStorage.get(key) || null;
    }
    try {
      return window.localStorage.getItem(key);
    } catch (e) {
      fallbackKeys.add(key);
      return memoryStorage.get(key) || null;
    }
  },
  setItem: (key, value) => {
    memoryStorage.set(key, value);
    if (hasGlobalStorage) {
      try {
        window.localStorage.setItem(key, value);
        fallbackKeys.delete(key);
      } catch (e) {
        fallbackKeys.add(key);
      }
    }
  },
  removeItem: (key) => {
    memoryStorage.delete(key);
    if (hasGlobalStorage) {
      try {
        window.localStorage.removeItem(key);
        fallbackKeys.delete(key);
      } catch (e) {
        fallbackKeys.add(key);
      }
    }
  }
};
