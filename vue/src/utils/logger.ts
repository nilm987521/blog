/**
 * 日誌服務 - 幫助在開發中更好地調試
 */

// 日誌等級
export enum LogLevel {
  ERROR = 0,
  WARN = 1,
  INFO = 2,
  DEBUG = 3
}

// 當前日誌等級 (可以從環境變量加載)
const currentLogLevel: LogLevel = LogLevel.DEBUG;

// 彩色日誌標題
const coloredLabel = {
  [LogLevel.ERROR]: '%cERROR',
  [LogLevel.WARN]: '%cWARN',
  [LogLevel.INFO]: '%cINFO',
  [LogLevel.DEBUG]: '%cDEBUG'
};

// 彩色日誌樣式
const coloredStyle = {
  [LogLevel.ERROR]: 'background: #FF5252; color: white; padding: 2px 4px; border-radius: 2px;',
  [LogLevel.WARN]: 'background: #FFC107; color: black; padding: 2px 4px; border-radius: 2px;',
  [LogLevel.INFO]: 'background: #2196F3; color: white; padding: 2px 4px; border-radius: 2px;',
  [LogLevel.DEBUG]: 'background: #4CAF50; color: white; padding: 2px 4px; border-radius: 2px;'
};

/**
 * 記錄錯誤日誌
 * @param message 日誌消息
 * @param data 可選數據
 */
export function logError(message: string, ...data: any[]): void {
  if (currentLogLevel >= LogLevel.ERROR) {
    console.error(coloredLabel[LogLevel.ERROR], coloredStyle[LogLevel.ERROR], message, ...data);
  }
}

/**
 * 記錄警告日誌
 * @param message 日誌消息
 * @param data 可選數據
 */
export function logWarn(message: string, ...data: any[]): void {
  if (currentLogLevel >= LogLevel.WARN) {
    console.warn(coloredLabel[LogLevel.WARN], coloredStyle[LogLevel.WARN], message, ...data);
  }
}

/**
 * 記錄信息日誌
 * @param message 日誌消息
 * @param data 可選數據
 */
export function logInfo(message: string, ...data: any[]): void {
  if (currentLogLevel >= LogLevel.INFO) {
    console.log(coloredLabel[LogLevel.INFO], coloredStyle[LogLevel.INFO], message, ...data);
  }
}

/**
 * 記錄調試日誌
 * @param message 日誌消息
 * @param data 可選數據
 */
export function logDebug(message: string, ...data: any[]): void {
  if (currentLogLevel >= LogLevel.DEBUG) {
    console.log(coloredLabel[LogLevel.DEBUG], coloredStyle[LogLevel.DEBUG], message, ...data);
  }
}

export default {
  error: logError,
  warn: logWarn,
  info: logInfo,
  debug: logDebug
}
