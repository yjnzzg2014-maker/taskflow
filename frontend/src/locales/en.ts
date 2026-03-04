export default {
  common: {
    loading: 'Loading...',
    save: 'Save',
    cancel: 'Cancel',
    confirm: 'Confirm',
    delete: 'Delete',
    edit: 'Edit',
    create: 'Create',
    search: 'Search',
    filter: 'Filter',
    export: 'Export',
    noData: 'No Data',
    success: 'Success',
    failed: 'Failed',
    language: 'Language'
  },
  search: {
    placeholder: 'Search tasks and events...',
    noResults: 'No results found',
    tasks: 'Tasks',
    events: 'Events',
    hint: 'Please enter at least 2 characters',
    tip: 'Press Ctrl+K to open search'
  },
  export: {
    title: 'Export Data',
    description: 'Export your data as iCal format to import into other calendar apps',
    calendar: 'Export Events',
    tasks: 'Export Tasks',
    all: 'Export All',
    success: 'Export successful',
    failed: 'Export failed'
  },
  auth: {
    login: 'Login',
    register: 'Register',
    logout: 'Logout',
    username: 'Username',
    password: 'Password',
    confirmPassword: 'Confirm Password',
    email: 'Email',
    loginSuccess: 'Login successful',
    registerSuccess: 'Register successful',
    loginFailed: 'Login failed',
    registerFailed: 'Register failed',
    noAccount: "Don't have an account?",
    hasAccount: 'Already have an account?',
    usernameLength: 'Username must be 3-50 characters',
    emailValid: 'Please input valid email',
    passwordLength: 'Password must be 6-100 characters',
    passwordMismatch: 'Passwords do not match'
  },
  nav: {
    dashboard: 'Dashboard',
    calendar: 'Calendar',
    tasks: 'Tasks',
    profile: 'Profile',
    tools: 'Tools'
  },
  dashboard: {
    title: 'Dashboard',
    totalTasks: 'Total Tasks',
    completedTasks: 'Completed',
    todayTasks: "Today's Tasks",
    overdueTasks: 'Overdue',
    taskStatus: 'Task Status',
    taskTrend: 'Task Trend',
    priorityDistribution: 'Priority Distribution',
    quickStats: 'Quick Stats',
    completionRate: 'Completion Rate',
    totalEvents: 'Total Events',
    todayEvents: "Today's Events",
    pendingTasks: 'Pending Tasks'
  },
  calendar: {
    title: 'Calendar',
    today: 'Today',
    newEvent: 'New Event',
    editEvent: 'Edit Event',
    eventTitle: 'Title',
    description: 'Description',
    allDay: 'All Day',
    startTime: 'Start Time',
    endTime: 'End Time',
    location: 'Location',
    remind: 'Remind',
    weekdays: {
      sun: 'Sun',
      mon: 'Mon',
      tue: 'Tue',
      wed: 'Wed',
      thu: 'Thu',
      fri: 'Fri',
      sat: 'Sat'
    },
    repeatTypes: {
      none: 'None',
      daily: 'Daily',
      weekly: 'Weekly',
      monthly: 'Monthly',
      yearly: 'Yearly',
      custom: 'Custom'
    },
    repeat: 'Repeat',
    repeatInterval: 'Interval (days)',
    repeatEndDate: 'End Date',
    views: {
      day: 'Day',
      week: 'Week',
      month: 'Month'
    }
  },
  markdown: {
    title: 'Markdown Tool',
    preview: 'Preview & Print',
    desc: 'Markdown text preview and print'
  },
  tags: {
    categories: 'Categories',
    tags: 'Tags',
    addCategory: 'Add Category',
    addTag: 'Add Tag',
    name: 'Name',
    color: 'Color'
  },
  shortcuts: {
    title: 'Keyboard Shortcuts',
    desc: 'View keyboard shortcuts',
    search: 'Open search',
    newTask: 'New task',
    togglePomodoro: 'Toggle Pomodoro',
    goDashboard: 'Go to Dashboard',
    goCalendar: 'Go to Calendar',
    goTasks: 'Go to Tasks'
  },
  task: {
    title: 'Tasks',
    newTask: 'New Task',
    editTask: 'Edit Task',
    taskTitle: 'Title',
    description: 'Description',
    priority: 'Priority',
    status: 'Status',
    dueDate: 'Due Date',
    category: 'Category',
    tags: 'Tags',
    subTasks: 'Sub Tasks',
    priorities: {
      low: 'Low',
      medium: 'Medium',
      high: 'High',
      urgent: 'Urgent'
    },
    sort: {
      priority: 'Priority',
      dueDate: 'Due Date',
      createdAt: 'Created',
      title: 'Title'
    },
    statuses: {
      pending: 'Pending',
      inProgress: 'In Progress',
      completed: 'Completed'
    },
    repeat: 'Repeat',
    repeatTypes: {
      none: 'None',
      daily: 'Daily',
      weekly: 'Weekly',
      monthly: 'Monthly',
      yearly: 'Yearly'
    },
    repeatInterval: 'Interval',
    repeatEndDate: 'End Date'
  },
  profile: {
    title: 'Profile',
    editProfile: 'Edit Profile',
    username: 'Username',
    email: 'Email',
    avatar: 'Avatar',
    avatarUrl: 'Avatar URL',
    memberSince: 'Member since',
    preferences: 'Preferences',
    darkMode: 'Dark Mode'
  },
  pomodoro: {
    title: 'Pomodoro',
    work: 'Work',
    shortBreak: 'Short Break',
    longBreak: 'Long Break',
    workComplete: 'Work complete! Take a break~',
    breakComplete: 'Break is over! Start working~',
    sessions: 'Sessions',
    settings: 'Timer Settings',
    workDuration: 'Work Duration',
    shortBreakDuration: 'Short Break',
    longBreakDuration: 'Long Break',
    longBreakInterval: 'Long Break Interval',
    autoStartBreaks: 'Auto Start Breaks',
    autoStartWork: 'Auto Start Work',
    sound: 'Sound',
    soundType: 'Sound Type',
    backgroundSound: 'Background Sound',
    volume: 'Volume',
    selectTask: 'Select Task',
    today: 'Today',
    todayMinutes: 'Today Mins',
    week: 'This Week'
  }
}
