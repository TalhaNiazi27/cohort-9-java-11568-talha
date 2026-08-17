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

let useMemoryStorage = !isStorageAvailable();

export const safeStorage = {
  getItem: (key) => {
    if (!useMemoryStorage) {
      try {
        return window.localStorage.getItem(key);
      } catch (e) {
        useMemoryStorage = true;
      }
    }
    return memoryStorage.get(key) || null;
  },
  setItem: (key, value) => {
    if (!useMemoryStorage) {
      try {
        window.localStorage.setItem(key, value);
        return;
      } catch (e) {
        useMemoryStorage = true;
      }
    }
    memoryStorage.set(key, value);
  },
  removeItem: (key) => {
    if (!useMemoryStorage) {
      try {
        window.localStorage.removeItem(key);
        return;
      } catch (e) {
        useMemoryStorage = true;
      }
    }
    memoryStorage.delete(key);
  }
};
