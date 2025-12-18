import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, ExternalLink, X, Clock, Calendar } from 'lucide-react';
import type { ClassAlert } from '../types/alert';

interface AlertOverlayProps {
    alert: ClassAlert | null;
    onDismiss: () => void;
}

/**
 * Full-screen alert overlay component.
 * 
 * Displays urgent class alerts with dramatic animations and visual effects.
 * Features:
 * - Full-screen red overlay
 * - Scale-in animation with bounce
 * - Pulsing background effect
 * - Large, impossible-to-miss text
 * - Action buttons for joining class or dismissing
 */
const AlertOverlay: React.FC<AlertOverlayProps> = ({ alert, onDismiss }) => {
    if (!alert) return null;

    const handleJoinClass = () => {
        if (alert.url) {
            window.open(alert.url, '_blank');
        }
        onDismiss();
    };

    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <AnimatePresence>
            {alert && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-50 flex items-center justify-center"
                >
                    {/* Pulsing background */}
                    <motion.div
                        animate={{
                            opacity: [0.95, 1, 0.95],
                        }}
                        transition={{
                            duration: 1.5,
                            repeat: Infinity,
                            ease: "easeInOut",
                        }}
                        className="absolute inset-0 bg-red-600"
                    />

                    {/* Alert content */}
                    <motion.div
                        initial={{ scale: 0, rotate: -10 }}
                        animate={{
                            scale: 1,
                            rotate: 0,
                        }}
                        exit={{ scale: 0, rotate: 10 }}
                        transition={{
                            type: "spring",
                            stiffness: 260,
                            damping: 20,
                        }}
                        className="relative z-10 max-w-4xl mx-4 p-12 bg-white rounded-3xl shadow-2xl"
                    >
                        {/* Close button */}
                        <button
                            onClick={onDismiss}
                            className="absolute top-6 right-6 p-2 rounded-full bg-gray-100 hover:bg-gray-200 transition-colors"
                            aria-label="Fechar alerta"
                        >
                            <X className="w-6 h-6 text-gray-600" />
                        </button>

                        {/* Alert icon */}
                        <motion.div
                            animate={{
                                scale: [1, 1.1, 1],
                                rotate: [0, -5, 5, 0],
                            }}
                            transition={{
                                duration: 0.5,
                                repeat: Infinity,
                                repeatDelay: 1,
                            }}
                            className="flex justify-center mb-8"
                        >
                            <div className="p-6 bg-red-100 rounded-full">
                                <AlertTriangle className="w-24 h-24 text-red-600" />
                            </div>
                        </motion.div>

                        {/* Title */}
                        <h1 className="text-5xl md:text-7xl font-black text-center text-red-600 mb-6 leading-tight">
                            🚨 AULA DETECTADA!
                        </h1>

                        {/* Class title */}
                        <h2 className="text-3xl md:text-4xl font-bold text-center text-gray-900 mb-8">
                            {alert.title}
                        </h2>

                        {/* Date and time */}
                        <div className="flex items-center justify-center gap-3 mb-8">
                            <Clock className="w-8 h-8 text-gray-600" />
                            <p className="text-2xl font-semibold text-gray-700">
                                {formatDate(alert.date)}
                            </p>
                        </div>

                        {/* Description */}
                        {alert.description && (
                            <p className="text-xl text-center text-gray-600 mb-10 max-w-2xl mx-auto">
                                {alert.description}
                            </p>
                        )}

                        {/* Action buttons */}
                        <div className="flex flex-col sm:flex-row gap-4 justify-center">
                            {alert.url && (
                                <motion.button
                                    whileHover={{ scale: 1.05 }}
                                    whileTap={{ scale: 0.95 }}
                                    onClick={handleJoinClass}
                                    className="flex items-center justify-center gap-3 px-10 py-5 bg-red-600 text-white text-2xl font-bold rounded-xl shadow-lg hover:bg-red-700 transition-colors"
                                >
                                    <ExternalLink className="w-8 h-8" />
                                    ENTRAR NA AULA
                                </motion.button>
                            )}

                            {alert.calendarLink && (
                                <motion.button
                                    whileHover={{ scale: 1.05 }}
                                    whileTap={{ scale: 0.95 }}
                                    onClick={() => window.open(alert.calendarLink!, '_blank')}
                                    className="flex items-center justify-center gap-3 px-10 py-5 bg-blue-600 text-white text-2xl font-bold rounded-xl shadow-lg hover:bg-blue-700 transition-colors"
                                >
                                    <Calendar className="w-8 h-8" />
                                    VER NO CALENDAR
                                </motion.button>
                            )}

                            <motion.button
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                                onClick={onDismiss}
                                className="px-10 py-5 bg-gray-200 text-gray-800 text-2xl font-bold rounded-xl shadow-lg hover:bg-gray-300 transition-colors"
                            >
                                Dispensar
                            </motion.button>
                        </div>

                        {/* Urgency indicator */}
                        {alert.isUrgent && (
                            <motion.div
                                animate={{
                                    opacity: [0.5, 1, 0.5],
                                }}
                                transition={{
                                    duration: 1,
                                    repeat: Infinity,
                                }}
                                className="mt-8 text-center"
                            >
                                <span className="inline-block px-6 py-3 bg-red-100 text-red-700 font-bold text-lg rounded-full">
                                    ⚠️ URGENTE - Não perca esta aula!
                                </span>
                            </motion.div>
                        )}
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default AlertOverlay;
