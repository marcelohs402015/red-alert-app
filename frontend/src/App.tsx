import { motion } from 'framer-motion';
import { Activity, Wifi, WifiOff, AlertCircle } from 'lucide-react';
import useRedAlertSocket from './hooks/useRedAlertSocket';
import AlertOverlay from './components/AlertOverlay';
import EmailList from './components/EmailList';
import AlertHistory from './components/AlertHistory';
import CategoryManager from './components/CategoryManager';
import type { ConnectionStatus } from './types/alert';

/**
 * Main application component.
 */
const App: React.FC = () => {
  const { connectionStatus, latestAlert, clearAlert } = useRedAlertSocket();

  const getStatusConfig = (status: ConnectionStatus) => {
    switch (status) {
      case 'connected':
        return {
          icon: Wifi,
          text: 'Monitorando Red-Alert System',
          color: 'text-green-500',
          bgColor: 'bg-green-500/10',
          borderColor: 'border-green-500/20',
        };
      case 'connecting':
        return {
          icon: Activity,
          text: 'Conectando ao Red-Alert System',
          color: 'text-yellow-500',
          bgColor: 'bg-yellow-500/10',
          borderColor: 'border-yellow-500/20',
        };
      case 'error':
        return {
          icon: AlertCircle,
          text: 'Erro na conexão - Tentando reconectar',
          color: 'text-red-500',
          bgColor: 'bg-red-500/10',
          borderColor: 'border-red-500/20',
        };
      default:
        return {
          icon: WifiOff,
          text: 'Desconectado do Red-Alert System',
          color: 'text-gray-500',
          bgColor: 'bg-gray-500/10',
          borderColor: 'border-gray-500/20',
        };
    }
  };

  const statusConfig = getStatusConfig(connectionStatus);
  const StatusIcon = statusConfig.icon;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <AlertOverlay alert={latestAlert} onDismiss={clearAlert} />

      <div className="container mx-auto px-4 py-8">
        <motion.header
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center mb-12"
        >
          <motion.h1
            className="text-6xl md:text-8xl font-black text-transparent bg-clip-text bg-gradient-to-r from-red-500 to-red-700 mb-4"
            style={{ backgroundSize: '200% 200%' }}
          >
            RED ALERT
          </motion.h1>
          <p className="text-xl md:text-2xl text-gray-400 font-medium">
            Sistema de Monitoramento de Emails
          </p>
        </motion.header>

        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2 }}
          className="max-w-2xl mx-auto mb-8"
        >
          <div className={`p-8 rounded-2xl border-2 ${statusConfig.bgColor} ${statusConfig.borderColor} backdrop-blur-sm`}>
            <div className="flex items-center justify-center gap-4 mb-6">
              <StatusIcon className={`w-12 h-12 ${statusConfig.color}`} />
              <h2 className={`text-3xl font-bold ${statusConfig.color}`}>
                {statusConfig.text}
              </h2>
            </div>

            {connectionStatus === 'connected' && (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-center">
                <p className="text-gray-400 text-lg">Aguardando notificações...</p>
                <div className="mt-4 inline-flex items-center gap-2 px-4 py-2 bg-slate-700/50 rounded-full">
                  <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse" />
                  <span className="text-sm text-gray-300">Ativo</span>
                </div>
              </motion.div>
            )}
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="mb-8"
        >
          <CategoryManager />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="grid lg:grid-cols-2 gap-6 mb-8"
        >
          <EmailList />
          <AlertHistory />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="grid md:grid-cols-3 gap-6 max-w-5xl mx-auto"
        >
          <InfoCard icon="📧" title="Monitoramento" description="Verifica emails a cada minuto" />
          <InfoCard icon="🤖" title="IA" description="Gemini AI analisa conteúdo" />
          <InfoCard icon="🔔" title="Alertas" description="Notificações em tempo real" />
        </motion.div>

        <motion.footer
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8 }}
          className="text-center mt-16 text-gray-500"
        >
          <p className="text-sm">Powered by Spring Boot + React + Gemini AI</p>
        </motion.footer>
      </div>
    </div>
  );
};

interface InfoCardProps {
  icon: string;
  title: string;
  description: string;
}

const InfoCard: React.FC<InfoCardProps> = ({ icon, title, description }) => (
  <motion.div
    whileHover={{ scale: 1.05, y: -5 }}
    className="p-6 bg-slate-800/50 backdrop-blur-sm rounded-xl border border-slate-700/50 hover:border-red-500/30 transition-all"
  >
    <div className="text-5xl mb-4 text-center">{icon}</div>
    <h3 className="text-xl font-bold text-white mb-2 text-center">{title}</h3>
    <p className="text-gray-400 text-center text-sm">{description}</p>
  </motion.div>
);

export default App;
