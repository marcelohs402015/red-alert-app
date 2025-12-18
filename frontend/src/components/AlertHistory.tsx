import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { History, Trash2, Calendar, ExternalLink } from 'lucide-react';
import { api } from '../services/api';
import type { ClassAlert } from '../types/alert';
import ConfirmModal from './ConfirmModal';

/**
 * Component to display alert history.
 */
const AlertHistory: React.FC = () => {
    const [alerts, setAlerts] = useState<ClassAlert[]>([]);
    const [loading, setLoading] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const loadHistory = async () => {
        setLoading(true);
        try {
            const response = await api.getAlertHistory(10);
            setAlerts(response.alerts);
        } catch (err) {
            console.error('Error loading alert history:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleClearHistory = () => {
        setIsModalOpen(true);
    };

    const confirmClear = async () => {
        try {
            await api.clearAlertHistory();
            setAlerts([]);
        } catch (err) {
            console.error('Error clearing history:', err);
        }
    };

    useEffect(() => {
        loadHistory();

        // Reload every 30 seconds
        const interval = setInterval(loadHistory, 30000);
        return () => clearInterval(interval);
    }, []);

    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <div className="bg-slate-800/50 backdrop-blur-sm rounded-xl border border-slate-700/50 p-6">
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h2 className="text-2xl font-bold text-white flex items-center gap-2">
                        <History className="w-6 h-6 text-purple-400" />
                        Histórico de Alertas
                    </h2>
                    <p className="text-gray-400 text-sm mt-1">
                        Últimos {alerts.length} alerta(s) recebido(s)
                    </p>
                </div>

                {alerts.length > 0 && (
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={handleClearHistory}
                        className="px-4 py-2 bg-red-600/20 text-red-400 font-semibold rounded-lg hover:bg-red-600/30 flex items-center gap-2"
                    >
                        <Trash2 className="w-4 h-4" />
                        Limpar
                    </motion.button>
                )}
            </div>

            <div className="space-y-3 max-h-96 overflow-y-auto">
                {loading && alerts.length === 0 && (
                    <div className="text-center py-12 text-gray-400">
                        Carregando...
                    </div>
                )}

                {!loading && alerts.length === 0 && (
                    <div className="text-center py-12 text-gray-400">
                        <History className="w-16 h-16 mx-auto mb-4 opacity-50" />
                        <p>Nenhum alerta no histórico</p>
                        <p className="text-sm mt-2">Alertas aparecerão aqui quando forem detectados</p>
                    </div>
                )}

                {alerts.map((alert, index) => (
                    <motion.div
                        key={index}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: index * 0.05 }}
                        className="p-4 bg-slate-700/30 rounded-lg border border-slate-600/30 hover:border-purple-500/30 transition-all"
                    >
                        <div className="flex items-start justify-between gap-4">
                            <div className="flex-1">
                                <div className="flex items-center gap-2 mb-2">
                                    <h3 className="font-semibold text-white">
                                        {alert.title}
                                    </h3>
                                    {alert.isUrgent && (
                                        <span className="px-2 py-0.5 bg-red-500/20 text-red-400 text-xs font-medium rounded-full">
                                            Urgente
                                        </span>
                                    )}
                                </div>

                                <p className="text-sm text-gray-300 mb-3">
                                    {alert.description}
                                </p>

                                <div className="flex items-center gap-4 text-xs text-gray-400">
                                    <div className="flex items-center gap-1">
                                        <Calendar className="w-3 h-3" />
                                        {formatDate(alert.date)}
                                    </div>

                                    {alert.url && (
                                        <a
                                            href={alert.url}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="flex items-center gap-1 text-blue-400 hover:text-blue-300"
                                        >
                                            <ExternalLink className="w-3 h-3" />
                                            Abrir link
                                        </a>
                                    )}

                                    {alert.calendarLink && (
                                        <a
                                            href={alert.calendarLink}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="flex items-center gap-1 text-purple-400 hover:text-purple-300"
                                        >
                                            <Calendar className="w-3 h-3" />
                                            Ver evento
                                        </a>
                                    )}
                                </div>
                            </div>
                        </div>
                    </motion.div>
                ))}
            </div>

            <ConfirmModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onConfirm={confirmClear}
                title="Limpar Histórico"
                message="Tem certeza que deseja limpar todo o histórico de alertas? Esta ação não pode ser desfeita."
                confirmText="Sim, limpar agora"
                cancelText="Cancelar"
                type="danger"
            />
        </div>
    );
};

export default AlertHistory;

