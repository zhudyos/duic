declare namespace Types {
  type NumStr = number | string
  type NullValue = null | undefined
  type FalseValue = boolean | void | undefined | null
  type Map<K extends string, V> = { [key in K]: V }
  type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>
  type Part<T, K extends keyof T> = Omit<T, K> & { [P in K]?: T[P] }
  type ObjectOf<T> = { [key: string]: T }
  type Extends<T> = T & { [key: string]: any }
  type Pair<T> = [T, T | undefined]
  type AnyFunc = (...args: any[]) => any
}
