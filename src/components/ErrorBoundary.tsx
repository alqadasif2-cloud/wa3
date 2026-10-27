import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('[ErrorBoundary caught error]:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-[#070b13] text-white flex flex-col items-center justify-center p-6 text-center select-none font-sans">
          <div className="w-16 h-16 rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-400 mb-4 shadow-lg shadow-red-500/10">
            <AlertTriangle className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold mb-2">تعذر عرض الواجهة</h2>
          <p className="text-slate-400 text-sm max-w-sm mb-6 leading-relaxed">
            حدث خطأ غير متوقع أثناء تشغيل واجهة المحفظة. يرجى الضغط على زر إعادة التحميل للمتابعة.
          </p>
          <button
            onClick={() => {
              this.setState({ hasError: false, error: null });
              window.location.reload();
            }}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 active:scale-95 text-white font-medium text-sm transition-all shadow-lg shadow-blue-600/30"
          >
            <RefreshCw className="w-4 h-4" />
            <span>إعادة تشغيل التطبيق</span>
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
