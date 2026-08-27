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

const hasStorage = isStorageAvailable();

export const safeStorage = {
  getItem: (key) => {
    if (hasStorage) {
      try {
        return window.localStorage.getItem(key);
      } catch (e) {
        return memoryStorage.get(key) || null;
      }
    }
    return memoryStorage.get(key) || null;
  },
  setItem: (key, value) => {
    if (hasStorage) {
      try {
        window.localStorage.setItem(key, value);
        return;
      } catch (e) {
        // Continue to fallback
      }
    }
    memoryStorage.set(key, value);
  },
  removeItem: (key) => {
    if (hasStorage) {
      try {
        window.localStorage.removeItem(key);
        return;
      } catch (e) {
        // Continue to fallback
      }
    }
    memoryStorage.delete(key);
  }
};
